package com.hyapp.achat.model;

public class Key {
        private String id;
        private byte rank;
        private int score;
        private long loginTime;

    public Key() {
    }

    public Key(String id, byte rank, int score, long loginTime) {
            this.id = id;
            this.rank = rank;
            this.score = score;
            this.loginTime = loginTime;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
