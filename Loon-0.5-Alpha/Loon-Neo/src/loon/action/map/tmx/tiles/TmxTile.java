package loon.action.map.tmx.tiles;

import java.util.ArrayList;
import java.util.List;

import loon.action.map.tmx.TMXProperties;
import loon.utils.xml.XMLElement;

public class TMXTile {
	
	private int id;
	private int totalDuration;

	private boolean animated;

	private List<TMXAnimationFrame> frames;

	private TMXProperties properties;

	public TMXTile() {
		this(0);
	}

	public TMXTile(int id) {
		this.id = id;

		frames = new ArrayList<>();
		properties = new TMXProperties();
	}

	public void parse(XMLElement element) {

		id = element.getIntAttribute("id", id);
		XMLElement nodes = element.getChildrenByName("properties");

		if (nodes != null) {
			properties.parse(nodes);
		}
		nodes = element.getChildrenByName("animation");
		
		if (nodes != null) {
			animated = true;
			ArrayList<XMLElement> tiles = nodes.list("frame");

			for (int i = 0; i < tiles.size(); i++) {
				XMLElement frame = tiles.get(i);

				int tileID = frame.getIntAttribute("tileid", 0);
				int duration = frame.getIntAttribute("duration", 0);

				TMXAnimationFrame animation = new TMXAnimationFrame(tileID,
						duration);
				frames.add(animation);
				totalDuration += duration;
			}
		}
	}

	public int getID() {
		return id;
	}

	public int getTotalDuration() {
		return totalDuration;
	}

	public int getFrameCount() {
		return frames.size();
	}

	public boolean isAnimated() {
		return animated;
	}

	public List<TMXAnimationFrame> getFrames() {
		return frames;
	}

	public TMXProperties getProperties() {
		return properties;
	}
}
