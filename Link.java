
public class Link {
	public int id;
	public cnnode.LinkType linkType;
	public double lossRate;
	public double linkWeight;
	
	public Link(int ID, cnnode.LinkType linkType, double p, double w) {
		id = ID;
		this.linkType = linkType;
		lossRate = p;
		linkWeight = w;
	}
}
