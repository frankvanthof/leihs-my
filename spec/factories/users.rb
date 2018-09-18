class User < Sequel::Model
  attr_accessor :password
end

FactoryBot.define do
  factory :user do
    firstname { Faker::Name.first_name }
    lastname { Faker::Name.last_name }
    email { firstname + '.' + lastname + '@' + Faker::Internet.domain_name }
    password { Faker::Internet.password(10, 20, true, true) }
    pw_hash  { database["SELECT crypt(#{database.literal(password)},gen_salt('bf')) " \
                        "AS pw_hash"].first[:pw_hash] }

    factory :admin do
      is_admin true
    end
  end
end
