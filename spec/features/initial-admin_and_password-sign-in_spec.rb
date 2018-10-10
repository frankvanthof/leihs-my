require 'spec_helper'
require 'pry'

feature 'Initial admin and password sign-in', type: :feature do
  scenario 'Create an initial admin and sign works ' do
    visit '/'

    # we get redirected to the initial admin because there are no admins yet
    expect(page).to have_content  "Initial Admin"

    # we create the initial admin
    fill_in 'email', with: 'admin@example.com'
    fill_in 'password', with: 'password'
    click_on 'Create'

    # we sign-in as the admin
    fill_in 'email', with: 'admin@example.com'

    click_on 'Continue'
    fill_in 'password', with: 'password'
    click_on 'Sign in'

    # we are signed-in
    wait_until do
      page.has_content? 'admin@example.com'
    end

    # we are still signed-in when we reload the page
    visit current_path
    wait_until do
      page.has_content? 'admin@example.com'
    end

    # sign-out
    click_on 'Sign out'
    expect(page).not_to have_content 'admin@example.com'

    # we are still signed-in when we reload the page
    visit current_path
    expect(page).not_to have_content 'admin@example.com'

  end
end