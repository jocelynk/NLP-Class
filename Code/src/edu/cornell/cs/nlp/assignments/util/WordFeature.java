package edu.cornell.cs.nlp.assignments.util;

/**
 * Created by User on 4/1/2016.
 */
public class WordFeature {
    private static final long serialVersionUID = 1L;
    Integer id;
    String word;
    String neTag;
    String posTag;
    Integer head;
    String depRelationship;

    public WordFeature(Integer id, String word, String neTag, String posTag, Integer head, String depRelationship) {
        this.id = id;
        this.word = word;
        this.neTag = neTag;
        this.posTag = posTag;
        this.head = head;
        this.depRelationship = depRelationship;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getNeTag() {
        return neTag;
    }

    public void setNeTag(String neTag) {
        this.neTag = neTag;
    }

    public String getPosTag() {
        return posTag;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag;
    }

    public Integer getHead() {
        return head;
    }

    public void setHead(Integer head) {
        this.head = head;
    }

    public String getDepRelationship() {
        return depRelationship;
    }

    public void setDepRelationship(String depRelationship) {
        this.depRelationship = depRelationship;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordFeature that = (WordFeature) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (word != null ? !word.equals(that.word) : that.word != null) return false;
        if (neTag != null ? !neTag.equals(that.neTag) : that.neTag != null) return false;
        if (posTag != null ? !posTag.equals(that.posTag) : that.posTag != null) return false;
        if (head != null ? !head.equals(that.head) : that.head != null) return false;
        return !(depRelationship != null ? !depRelationship.equals(that.depRelationship) : that.depRelationship != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (word != null ? word.hashCode() : 0);
        result = 31 * result + (neTag != null ? neTag.hashCode() : 0);
        result = 31 * result + (posTag != null ? posTag.hashCode() : 0);
        result = 31 * result + (head != null ? head.hashCode() : 0);
        result = 31 * result + (depRelationship != null ? depRelationship.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "WordFeature{" +
                "id=" + id +
                ", word='" + word + '\'' +
                ", neTag='" + neTag + '\'' +
                ", posTag='" + posTag + '\'' +
                ", head=" + head +
                ", depRelationship='" + depRelationship + '\'' +
                '}';
    }

}
