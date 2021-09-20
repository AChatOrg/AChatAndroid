package com.hyapp.achat.model;

public class Key {
        private String ipv4;
        private int rank;
        private int score;
        private long loginTime;

        public Key(String ipv4, int rank, int score, long loginTime) {
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

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
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
