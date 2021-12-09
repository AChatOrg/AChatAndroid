package com.hyapp.achat.model;

public class Key {
        private String ipv4;
        private byte rank;
        private int score;
        private long loginTime;

    public Key() {
    }

    public Key(String ipv4, byte rank, int score, long loginTime) {
            this.ipv4 = ipv4;
            this.rank = rank;
            this.score = score;
            this.loginTime = loginTime;
        }

        public String getIpv4() {
            return ipv4;
        }

        public void setIpv4(String ipv4) {
            this.ipv4 = ipv4;
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
