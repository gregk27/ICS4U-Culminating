package ca.gkelly.engine.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ca.gkelly.engine.collision.Collider;
import ca.gkelly.engine.collision.PolyCollider;
import ca.gkelly.engine.util.Logger;
import ca.gkelly.engine.util.Tools;
import ca.gkelly.engine.util.Vertex;

/** Class used to load and render a .tmx tilemap */
public class TileMap {

	BufferedImage[] tiles;
	String src;

	int[][] map;

	public Document doc;

	public Tileset tileset;

	/** Complete render of the map, rendered on load then saved */
	BufferedImage image;
	/**
	 * Chunk removed from <code>image</code> used for rendering
	 */
	BufferedImage cameraRender;

	/** Last Top-Left position of camera, used to check need for re-render */
	int[] lastTL = { -1, -1 };
	/** Last Bottom-Right position of camera, used to check need for re-render */
	int[] lastBR = { -1, -1 };

	/** Polygon collider layers used on map */
	ColliderLayer[] colliders;

	/**
	 * Prepare a new tiled map
	 * 
	 * @param src The path to the <code>.tmx</code> file
	 */
	public TileMap(String src) {
		this.src = src;
	}

	/**
	 * Load the tiled map, and all associate resources, into memory This can be used
	 * to create many maps on startup, but only load them as needed
	 */
	public boolean load() {
		File file = new File(src);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		// Load the file, if it fails, return false
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(file);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// Prepare document
		doc.getDocumentElement().normalize();

		// Get important elements from map
		Element tilesetElement = (Element) doc.getElementsByTagName("tileset").item(0);
		Element mapData = (Element) doc.getElementsByTagName("data").item(0);

		// Prepare the string that contains the map details
		String mapString = mapData.getTextContent().replaceFirst("\n", "");
		mapString = mapString.substring(0, mapString.length() - 1);
		String[] rows = mapString.split(",\n");
		Logger.log(Logger.DEBUG, mapString);

		map = new int[rows[0].split(",").length][rows.length];

		// Get integers values for each tile
		for(int y = 0;y < rows.length;y++) {
			String[] tiles = rows[y].split(",");
			for(int x = 0;x < tiles.length;x++) {
				Logger.log(Logger.DEBUG, tiles[x], "", true);
				map[x][y] = Integer.parseInt(tiles[x]);
			}
			Logger.newLine(Logger.DEBUG);
		}

		// Load the tileset
		try {
			tileset = new Tileset(tilesetElement, src);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Logger.newLine(Logger.DEBUG);
		Logger.newLine(Logger.DEBUG);

		// Create full render image
		image = new BufferedImage(map.length * tileset.tWidth, map[0].length * tileset.tHeight,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();
		for(int x = 0;x < map.length;x++) {
			for(int y = 0;y < map[0].length;y++) {
				try {
					g.drawImage(tileset.getImage(map[x][y]), x * tileset.tWidth, y * tileset.tHeight, null);
				} catch (IndexOutOfBoundsException e) {

				}
			}
		}

		// Get collider elements
		NodeList layers = (NodeList) doc.getElementsByTagName("objectgroup");
		colliders = new ColliderLayer[layers.getLength()];

		Logger.log(Logger.DEBUG, layers.getLength());

		for(int i = 0;i < layers.getLength();i++) {
			Element e = (Element) layers.item(i);
			NodeList polyNodes = (NodeList) e.getElementsByTagName("object");
			colliders[i] = new ColliderLayer(polyNodes, e.getAttribute("name"));
		}

		return true;
	}

	/**
	 * Get a cropped version of the map
	 * 
	 * @param tl Top-left
	 * @param br Bottom-right
	 * 
	 * @return Array containing:<br/>
	 *         - <strong>[0]:</strong> {@link BufferedImage} Cropped Image<br/>
	 *         - <strong>[1]:</strong> {@link int} X offset<br/>
	 *         - <strong>[2]:</strong> {@link int} Y offset
	 */
	public Object[] render(int[] tl, int[] br) {

		int margin = 10;
		tl[0] = Tools.minmax(tl[0], margin, image.getWidth() - margin);
		br[0] = Tools.minmax(br[0], margin, image.getWidth() - margin);
		tl[1] = Tools.minmax(tl[1], margin, image.getHeight() - margin);
		br[1] = Tools.minmax(br[1], margin, image.getHeight() - margin);

		// If the values have changed, re-crop the image
		// Otherwise just use existing
		if(!(Arrays.equals(lastTL, tl) && Arrays.equals(lastBR, br))) {
			lastTL = tl;
			lastBR = br;
//			Logger.log("Re-render");
			cameraRender = image.getSubimage(tl[0] - margin, tl[1] - margin, br[0] - tl[0] + 2 * margin,
					br[1] - tl[1] + 2 * margin);
		}

		return (new Object[] { cameraRender, tl[0] - margin, tl[1] - margin });
	}

	/**
	 * Get the image associate with a specific tile ID
	 * 
	 * @param ID The ID of the tile
	 * @return The image associated with the selected tile
	 */
	public BufferedImage getTile(int ID) {
		return (tileset.getImage(ID));
	}

	/**
	 * Get the polygon that contains the point, from the selected layer
	 * 
	 * @param x     The x value of the point
	 * @param y     The y value of the point
	 * @param layer The layer to search
	 * @return The polygon that contains the point<br/>
	 *         <strong>null</strong> if no polygon contains the point
	 *         <strong>null</strong> if the layer doesn't exist
	 */
	public Polygon getPoly(int x, int y, String layer) {
		for(ColliderLayer c: colliders) {
			if(c.name.equals(layer)) {
				return (c.getPoly(x, y));
			}
		}
		return null;
	}

	public Collider[] getColliders(String layer) {
		for(ColliderLayer c: colliders) {
			if(c.name.equals(layer)) {
				return c.polygons;
			}
		}
		return null;
	}

}

/** A class used to handle the colliders on a map */
class ColliderLayer {
	public PolyCollider[] polygons;
	String name;

	/**
	 * Create the collider layer
	 * 
	 * @param polys The list of polygons
	 * @param name  The name to used for identification
	 */
	ColliderLayer(NodeList polys, String name) {
		this.name = name;
		polygons = new PolyCollider[polys.getLength()];
		for(int j = 0;j < polys.getLength();j++) {
			Element poly = (Element) polys.item(j);
			int x = Integer.parseInt(poly.getAttribute("x"));
			int y = Integer.parseInt(poly.getAttribute("y"));

			poly = (Element) poly.getElementsByTagName("polygon").item(0);
			String[] points = poly.getAttribute("points").split(" ");
			// Create int[]s for x and y points
			Vertex[] vertices = new Vertex[points.length];
			for(int s = 0;s < points.length;s++) {
				vertices[s] = new Vertex((Integer.parseInt(points[s].split(",")[0])) + x,
						(Integer.parseInt(points[s].split(",")[1])) + y);
			}
			// Create the corresponding polygon
			polygons[j] = new PolyCollider(vertices);
		}
	}

	/**
	 * Draw the polygons to the screen
	 * 
	 * @param cam The camera to use
	 * @param c   The colour to use
	 */
	public void render(Camera cam, Color c) {
		for(PolyCollider collider: polygons) {
			cam.drawPoly(collider.getPoly(), c);
		}
	}

	/**
	 * Get the polygon that contains the point
	 * 
	 * @param x The x value of the point
	 * @param y The y value of the point
	 * @return The polygon that contains the point<br/>
	 *         <strong>null</strong> if no polygon contains the point
	 */
	public Polygon getPoly(int x, int y) {
		for(PolyCollider p: polygons) {
			if(p.contains(x, y)) return p.getPoly();
		}
		return null;
	}
}

/** A class used to handle loading and management of a collider layer */
class Tileset {
	/** Width of a tile */
	int tWidth;
	/** Height of a tile */
	int tHeight;
	/** Number of tiles in the set */
	private int tCount;
	/** Number of columns in the source */
	private int columns;

	private BufferedImage[] tiles;

	/**
	 * Load a tileset
	 * 
	 * @param e    The tileset xml element
	 * @param path The path to the tileset image
	 */
	Tileset(Element e, String path) throws IOException {
		// Get attributes from xml
		tWidth = Integer.parseInt(e.getAttribute("tilewidth"));
		tHeight = Integer.parseInt(e.getAttribute("tileheight"));
		tCount = Integer.parseInt(e.getAttribute("tilecount"));
		columns = Integer.parseInt(e.getAttribute("columns"));
//		String subPath = ((Element) e.getFirstChild()).getAttribute("source");
		String subPath = "tiles.png"; // TODO: Why?

		tiles = new BufferedImage[tCount];
		Logger.log(path.substring(0, path.lastIndexOf("\\")) + "\\" + subPath);
		BufferedImage sheet = ImageIO.read(new File(path.substring(0, path.lastIndexOf("\\")) + "\\" + subPath));

		// Load tiles from image
		for(int y = 0;y < sheet.getHeight() / tHeight;y++) {
			for(int x = 0;x < columns;x++) {
				Logger.log(Logger.DEBUG, x * tWidth + "\t" + y * tHeight + "\t" + (x + (columns * y)));
				tiles[x + (columns * y)] = sheet.getSubimage(x * tWidth, y * tHeight, tWidth, tHeight);
			}
		}

	}

	/**
	 * Get the image associate with a specific tile ID
	 * 
	 * @param ID The ID of the tile
	 * @return The image associated with the selected tile
	 */
	BufferedImage getImage(int ID) {
		return tiles[ID - 1];
	}
}
