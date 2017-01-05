package in.co.viditkothari.bookswap;

/**
 * Created by viditkothari on 04-Jan-17.
 */

class Book {

    private String mImg;
    private String mTitle;
    private String mAuthor;
    private String mISBN;
    private String mDesc;
    private String mInfoLink;

    Book(String mImg, String mTitle, String mAuthor, String mISBN, String mDesc, String mInfoLink) {
        this.mImg = mImg;
        this.mTitle = mTitle;
        this.mAuthor = mAuthor;
        this.mISBN = mISBN;
        this.mDesc = mDesc;
        this.mInfoLink = mInfoLink;
    }

    public String getmImg() {
        return mImg;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getmISBN() {
        return mISBN;
    }

    public String getmDesc() {
        return mDesc;
    }

    public String getmInfoLink() {
        return mInfoLink;
    }
}
