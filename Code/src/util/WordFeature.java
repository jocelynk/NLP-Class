package util;

/**
 * Created by User on 4/1/2016.
 */
public class WordFeature {
    private static final long serialVersionUID = 1L;
    String word;
    String posTag;
    String chunkTag;
    String neTag;

    public WordFeature(String word, String posTag, String chunkTag, String neTag) {
        this.word = word;
        this.posTag = posTag;
        this.chunkTag = chunkTag;
        this.neTag = neTag;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPosTag() {
        return posTag;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag;
    }

    public String getChunkTag() {
        return chunkTag;
    }

    public void setChunkTag(String chunkTag) {
        this.chunkTag = chunkTag;
    }

    public String getNeTag() {
        return neTag;
    }

    public void setNeTag(String neTag) {
        this.neTag = neTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordFeature that = (WordFeature) o;

        if (word != null ? !word.equals(that.word) : that.word != null) return false;
        if (posTag != null ? !posTag.equals(that.posTag) : that.posTag != null) return false;
        if (chunkTag != null ? !chunkTag.equals(that.chunkTag) : that.chunkTag != null) return false;
        return !(neTag != null ? !neTag.equals(that.neTag) : that.neTag != null);

    }

    @Override
    public int hashCode() {
        int result = word != null ? word.hashCode() : 0;
        result = 31 * result + (posTag != null ? posTag.hashCode() : 0);
        result = 31 * result + (chunkTag != null ? chunkTag.hashCode() : 0);
        result = 31 * result + (neTag != null ? neTag.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WordFeature{" +
                "word='" + word + '\'' +
                ", posTag='" + posTag + '\'' +
                ", chunkTag='" + chunkTag + '\'' +
                ", neTag='" + neTag + '\'' +
                '}';
    }
}
