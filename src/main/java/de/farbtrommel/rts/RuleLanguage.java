package de.farbtrommel.rts;

import java.io.Serializable;

/**
 * This class represents a rule language.
 */
public class RuleLanguage implements Serializable {
    private String name;
    private String version;
    private String mimeType;
    private String description;

    /**
     * Constructor
     *
     * @param name    The name of the rule language
     * @param version The version of the rule language
     */
    public RuleLanguage(String name, String version, String mimeType) {
        this.name = name;
        this.version = version;
        this.mimeType = mimeType;
    }

    /**
     * Getter for the name of the rule language
     *
     * @return The name as a string
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the name of the rule language
     *
     * @param name The name to be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the version of the rule language
     *
     * @return The version of the rule language
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter for the version of the rule language
     *
     * @param version The version to be set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Getter for the description of the rule language
     *
     * @return The description of the rule language
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for the description of the rule language
     *
     * @param description The description to be set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RuleLanguage other = (RuleLanguage) obj;
        if (name == null && other.name != null) {
            return false;
        } else if (!name.trim().toLowerCase().equals(other.name.trim().toLowerCase())) {
            return false;
        } else if (version == null && other.version != null) {
            return false;
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuleLanguage [name=" + name + ", version=" + version + "]";
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
