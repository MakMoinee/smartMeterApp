package com.thesis.smesurviveapp.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Users {
    private int userID;
    private String username;
    private String password;
    private String userType;

    public Users(UserBuilder builder) {
        this.userID = builder.userID;
        this.username = builder.username;
        this.password = builder.password;
        this.userType = builder.userType;
    }

    public static class UserBuilder {
        private int userID;
        private String username;
        private String password;
        private String userType;

        public UserBuilder setUserID(int userID) {
            this.userID = userID;
            return this;
        }

        public UserBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder setUserType(String userType) {
            this.userType = userType;
            return this;
        }

        public Users build() {
            return new Users(this);
        }
    }
}
