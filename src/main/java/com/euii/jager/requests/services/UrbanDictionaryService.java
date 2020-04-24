package com.euii.jager.requests.services;

import java.util.List;

public class UrbanDictionaryService {

    private List<String> tags;
    private List<UrbanDictionaryObject> list;

    public List<String> getTags() {
        return tags;
    }

    public List<UrbanDictionaryObject> getList() {
        return list;
    }

    public boolean hasData() {
        return getList() != null && !getList().isEmpty();
    }

    public class UrbanDictionaryObject {

        private int definitonId;
        private int thumbsUp;
        private int thumbsDown;
        private String definition;
        private String permalink;
        private String author;
        private String word;
        private String example;

        public int getDefinitonId() {
            return definitonId;
        }

        public int getThumbsUp() {
            return thumbsUp;
        }

        public int getThumbsDown() {
            return thumbsDown;
        }

        public String getDefinition() {
            return definition;
        }

        public String getPermalink() {
            return permalink;
        }

        public String getAuthor() {
            return author;
        }

        public String getWord() {
            return word;
        }

        public String getExample() {
            return example;
        }
    }
}
