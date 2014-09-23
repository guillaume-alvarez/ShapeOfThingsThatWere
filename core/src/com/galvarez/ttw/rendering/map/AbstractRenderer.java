package com.galvarez.ttw.rendering.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class AbstractRenderer {
	
	protected OrthographicCamera camera;
	protected SpriteBatch batch;
	
	public AbstractRenderer(OrthographicCamera camera, SpriteBatch batch) {
		this.camera = camera;
		this.batch = batch;
	}
	
	protected void begin() {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
	}
	
	protected void end() {
		batch.end();
		batch.setColor(1f,1f,1f,1f);
	}
	
}
