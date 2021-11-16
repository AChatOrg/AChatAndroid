package com.hyapp.achat.model;

public class Key {
        private String uuid;
        private byte rank;
        private int score;
        private long loginTime;

    public Key() {
    }

    public Key(String uuid, byte rank, int score, long loginTime) {
            this.uuid = uuid;
            this.rank = rank;
            this.score = score;
            this.loginTime = loginTime;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public byte getRank() {
            return rank;
        }

        public void setRank(byte rank) {
            this.rank = rank;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public long getLoginTime() {
            return loginTime;
        }

        public void setLoginTime(long loginTime) {
            this.loginTime = loginTime;
        }
    }
