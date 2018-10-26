package com.fatihsevban.instakotlinapp.Utils;

import com.fatihsevban.instakotlinapp.Models.Post;
import com.fatihsevban.instakotlinapp.Models.User;

public class EventBusDataEvent {

    public static class SendEmailData {

        // properties
        private String email;

        // constructor
        public SendEmailData(String email) {
            this.email = email;
        }

        public SendEmailData() {
        }

        // methods
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class SendPhoneData {

        // properties
        private String phoneNumber;

        // constructor
        public SendPhoneData(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public SendPhoneData() {
        }

        // methods
        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class SendPhoneRecordData {

        // properties
        private String phoneNumber;
        private String verificationID;
        private String authCode;

        // constructor
        public SendPhoneRecordData(String phoneNumber, String verificationID, String authCode) {
            this.phoneNumber = phoneNumber;
            this.verificationID = verificationID;
            this.authCode = authCode;
        }

        // methods
        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getVerificationID() {
            return verificationID;
        }

        public void setVerificationID(String verificationID) {
            this.verificationID = verificationID;
        }

        public String getAuthCode() {
            return authCode;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }
    }

    public static class SendCurrentUserData {

        // properties
        private User user;

        // constructor
        public SendCurrentUserData(User user) {
            this.user = user;
        }

        // methods
        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    public static class SendImageUriData {

        // properties
        private String uri;
        private String docType;

        // constructor

        public SendImageUriData(String uri, String docType) {
            this.uri = uri;
            this.docType = docType;
        }

        // methods

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }
    }

    public static class SendProfileImageData {

        // properties
        private String profileImageUri;

        // constructor
        public SendProfileImageData(String profileImageUri) {
            this.profileImageUri = profileImageUri;
        }

        // methods
        public String getProfileImageUri() {
            return profileImageUri;
        }

        public void setProfileImageUri(String profileImageUri) {
            this.profileImageUri = profileImageUri;
        }
    }

    public static class SendPostIdData {

        // properties
        private String postId;

        // constructor
        public SendPostIdData(String postId) {
            this.postId = postId;
        }

        // methods
        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }
    }

    public static class SendPostData {

        // properties
        private Post post;

        // constructor
        public SendPostData(Post post) {
            this.post = post;
        }

        // methods
        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    public static class SendProfileData {

        // properties
        private User profileData;

        // constructor
        public SendProfileData(User profileData) {
            this.profileData = profileData;
        }

        // methods
        public User getProfileData() {
            return profileData;
        }

        public void setProfileData(User profileData) {
            this.profileData = profileData;
        }
    }

    public static class SendVideoUriData {

        // properties
        private String videoUri;

        // constructor
        public SendVideoUriData(String videoUri) {
            this.videoUri = videoUri;
        }

        // methods
        public String getVideoUri() {
            return videoUri;
        }

        public void setVideoUri(String videoUri) {
            this.videoUri = videoUri;
        }
    }

    public static class SendChatUsersData {

        // properties
        private User contactUserData;
        private User currentUserData;

        // constructor
        public SendChatUsersData(User contactUserData, User currentUserData) {
            this.contactUserData = contactUserData;
            this.currentUserData = currentUserData;
        }


        // methods
        public User getContactUserData() {
            return contactUserData;
        }

        public void setContactUserData(User contactUserData) {
            this.contactUserData = contactUserData;
        }

        public User getCurrentUserData() {
            return currentUserData;
        }

        public void setCurrentUserData(User currentUserData) {
            this.currentUserData = currentUserData;
        }
    }


}