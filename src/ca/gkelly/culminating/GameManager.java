package ca.gkelly.culminating;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import ca.gkelly.culminating.resources.Bullet;
import ca.gkelly.culminating.resources.PlayerResource;
import ca.gkelly.engine.Manager;
import ca.gkelly.engine.graphics.Camera;
import ca.gkelly.engine.graphics.TileMap;
import ca.gkelly.engine.loader.Loader;
import ca.gkelly.engine.util.Logger;
import ca.gkelly.engine.util.Vector;

public class GameManager extends Manager {

	TileMap map;
	Camera cam;
	Player player;
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();

	public GameManager(String[] args) {
		map = new TileMap(args[0] + "\\maps\\map.tmx");
	}

	@Override
	public void init(Container container) {
		map.load();
		cam = new Camera(container, map);
		cam.setPosition(0, 0, 1.25);

		player = new Player((PlayerResource) Loader.resources.get("player").get(0), 0, 0);
	}

	@Override
	public void render(Graphics g) {
		cam.begin();
		player.render(cam);

		for(Bullet b: new ArrayList<Bullet>(bullets)) {
			b.render(cam);
		}
		
		int[] pos = cam.worldSpace(mouse.pos.x, mouse.pos.y);
		Polygon p = map.getPoly(pos[0], pos[1], "colliders");
		if(p!=null) {
			
		}

		cam.drawRect(player.getRectX(), player.getRectY(), player.getWidth(), player.getHeight(), Color.blue);

		cam.finish(g);
		Logger.newLine(Logger.DEBUG);
	}

	@Override
	public void update() {
		player.update();
		for(Bullet b: new ArrayList<Bullet>(bullets)) {
			b.update();
			Polygon p = map.getPoly(b.getX(), b.getY(), "colliders");
			if(p!=null) {
				cam.drawPoly(p, Color.blue);
			}
		}

		if(keyboard.pressed.contains(KeyEvent.VK_W))
			player.move(0, -1.0);
		if(keyboard.pressed.contains(KeyEvent.VK_S))
			player.move(0, 1.0);
		if(keyboard.pressed.contains(KeyEvent.VK_A))
			player.move(-1.0, 0);
		if(keyboard.pressed.contains(KeyEvent.VK_D))
			player.move(1.0, 0);

		if(keyboard.pressed.contains(KeyEvent.VK_Q))
			cam.zoom(0.05);
		if(keyboard.pressed.contains(KeyEvent.VK_E))
			cam.zoom(-0.05);

		Logger.log(player.getVelocity().getString(Vector.STRING_RECTANGULAR));
		cam.setPosition((int) player.x, (int) player.y);
	}

	@Override
	public void end() {

	}

	@Override
	public void onMousePress(MouseEvent e) {
		int[] pos = cam.worldSpace(e.getX(), e.getY());
		// Logger.log(Logger.INFO, new
		// Vector(pos[0]-player.x,pos[1]-player.y).normalized().getString(Vector.STRING_RECTANGULAR));

		if(player.contains(pos[0], pos[1])) {
			return;
		}

		Vector vel = new Vector(pos[0] - player.x, pos[1] - player.y);
		vel.setMag(3);
		
		Vector extraVel = player.getVelocity().getAtAngle(vel.getAngle(false));
		bullets.add(new Bullet((int) player.x, (int) player.y, vel, extraVel));
	}

}
