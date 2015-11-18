package hello;

public class Greeting {

/*    private final long id;
    private final String content;

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }*/
	
	String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	String uuid;
	String socialmediaType;
	String child_user_id;
	String parent_id;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getSocialmediaType() {
		return socialmediaType;
	}
	public void setSocialmediaType(String socialmediaType) {
		this.socialmediaType = socialmediaType;
	}
	public String getChild_user_id() {
		return child_user_id;
	}
	public void setChild_user_id(String child_user_id) {
		this.child_user_id = child_user_id;
	}
	public String getParent_id() {
		return parent_id;
	}
	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}
	
	
}
