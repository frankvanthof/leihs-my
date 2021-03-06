require 'capybara/rspec'
require 'faraday'
require 'faraday_middleware'
require 'pathname'
require 'pry'
require 'selenium-webdriver'
require 'fileutils'

def base_url
  @base_url ||= ENV['LEIHS_MY_HTTP_BASE_URL'].presence || 'http://localhost:3240'
end

def port
  @port ||= Addressable::URI.parse(base_url).port
end

def plain_faraday_json_client
  @plain_faraday_json_client ||= Faraday.new(
    url: base_url,
    headers: { accept: 'application/json' }) do |conn|
      conn.adapter Faraday.default_adapter
      conn.response :json, content_type: /\bjson$/
    end
end

def set_capybara_values
  Capybara.app_host = base_url
  Capybara.server_port = port
end

def set_browser(example)
  Capybara.current_driver = \
    begin
      ENV['CAPYBARA_DRIVER'].presence.try(:to_sym) \
          || example.metadata[:driver] \
          || :selenium
    rescue
      :selenium
    end
end

RSpec.configure do |config|
  Capybara.current_driver = :selenium
  set_capybara_values

  if ENV['FIREFOX_ESR_45_PATH'].present?
    Selenium::WebDriver::Firefox.path = ENV['FIREFOX_ESR_45_PATH']
  end

  config.before :all do
    set_capybara_values
  end

  config.before :each do |example|
    set_capybara_values
    set_browser example
  end


  config.after(:each) do |example|
    unless example.exception.nil?
      take_screenshot screenshot_dir
    end
  end

  config.before :all do 
    FileUtils.remove_dir(screenshot_dir, force: true)
    FileUtils.mkdir_p(screenshot_dir)
  end

  def tmp_dir
    Pathname File.absolute_path(Pathname(__FILE__).join("..","..","..","tmp"))
  end

  def screenshot_dir
    tmp_dir.join('screenshots')
  end

  def take_screenshot(screenshot_dir = nil, name = nil)
    name ||= "#{Time.now.iso8601.tr(':', '-')}.png"
    path = screenshot_dir.join(name)
    case Capybara.current_driver
    when :selenium
      page.driver.browser.save_screenshot(path) rescue nil
    when :poltergeist
      page.driver.render(path, full: true) rescue nil
    else
      Logger.warn "Taking screenshots is not implemented for \
              #{Capybara.current_driver}."
    end
  end


end


def plain_faraday_client
  Faraday.new(
    url: base_url,
    headers: { accept: 'application/json' }) do |conn|
      conn.adapter Faraday.default_adapter
      conn.response :json, content_type: /\bjson$/
    end
end
