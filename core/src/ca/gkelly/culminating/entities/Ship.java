package ca.gkelly.culminating.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import ca.gkelly.culminating.loader.VesselSource;

public class Ship extends Entity{

	Mount[] mounts;
	
	Sprite sprite;
	
	public Ship(VesselSource v, int x, int y, Mount[] m) {
		super(x,y,v.texture);
		mounts = m;
	}

	@Override
	public void render(SpriteBatch b) {
		if(sprite == null) reRender();
		for(Mount m : mounts) {
			if(m.getRenderRequest()) {
				reRender();
			}
		}
		b.draw(sprite, x, y);
	}
	
	private void reRender() {
		
		SpriteBatch b = new SpriteBatch();
		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, 64,32,false);
		
		fbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        b.begin();
		b.draw(texture, 0,0);
		for(Mount m : mounts) {
			m.render(b);
		}
		b.end();
		
		fbo.end();
		
		sprite = new Sprite(fbo.getColorBufferTexture());
		sprite.flip(false,  true);
		
		rect = new Rectangle(x, y, sprite.getWidth(), sprite.getHeight());
		System.out.println("SPRITE:"+sprite.getWidth() +"\t"+ sprite.getHeight());
	}

	@Override
	public void update() {
		rect.setPosition(new Vector2(x,y));
	}
	
	public void move(int x, int y, MapObjects terrain) {
		boolean canMove = true;
		for(PolygonMapObject p : terrain.getByType(PolygonMapObject.class)) {
			if(p.getPolygon().contains(new Vector2(x, y))) {
				canMove = false;
			}
		}
		
	}
	
}
