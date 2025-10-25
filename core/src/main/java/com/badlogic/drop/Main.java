package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;

    SpriteBatch spriteBatch;
    FitViewport viewport;

    Sprite bucketSprite; // Declare a new Sprite variable

    Vector2 touchPos;

    Array<Sprite> dropSprites; // list which adds the drops that are created

    float dropTimer;

    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    @Override
    public void create() {
        // Prepare your application here.
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        bucketSprite = new Sprite(bucketTexture); // initialize the sprite based on the texture
        bucketSprite.setSize(1, 1); // define the size of the sprite

        touchPos = new Vector2();

//        creating an array to store droplet sprites
        dropSprites = new Array<>();

//        creating Rectangle objects for the bucket and the drop for the purpose of collision detection
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

//        setting continuous music
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if (width <= 0 || height <= 0) return;

        // Resize your application here. The parameters represent the new window size.
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        // Draw your application here.
        input();
        logic();
        draw();
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            // todo: Do something when the user presses the right arrow
            bucketSprite.translateX(speed * delta); // move the bucket right
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            // todo: Do something when the user presses the left arrow
            bucketSprite.translateX(-speed * delta); // move the bucket left
        }

        if (Gdx.input.isTouched()) { // if the user has clicked or tapped the screen
            // todo: React to the player touching the screen
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // Get where the touch happened on the screen
            viewport.unproject(touchPos); // we convert the units to the world units of the viewport
            bucketSprite.setCenterX(touchPos.x); // change the horizontally centered position of the bucket
        }
    }

    private void logic() {
        // store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // store the bucket size for brevity
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        // clamp x to values between 0 and worldWidth
        // also we subtract the bucket width
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime();
        // Apply the bucket position and size to the bucketRectangle
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);
/*
        we remove the below loop to prevent drop sprite objects from hanging up the system
        loop through each drop
        for (Sprite dropSprite : dropSprites) {
            dropSprite.translateY(-2f * delta); // move the drop downwards every frame
        }
*/
        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i); // Get the sprite from the list
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta);
            // Apply the drop position and size to the dropRectangle
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            // if the top of the drop goes below the bottom of the view, remove it
            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);
            else if (bucketRectangle.overlaps(dropRectangle)) { // check if the drop overlaps the bucket
                dropSprites.removeIndex(i);
                dropSound.play(); // play the sound when the bucket meets the drop
            }
        }

//        creating drop only after every second of delta time
        dropTimer += delta; // adding the current delta to the timer
        if (dropTimer > 1f) { // if the timer exceeds over a second
            dropTimer = 0; // we reset the timer to zero
            createDroplet(); // and we create the droplet
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        // add lines to draw stuff here

        // store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight); // draw the background
//        spriteBatch.draw(bucketTexture, 0, 0, 1, 1); // draw the bucket
        bucketSprite.draw(spriteBatch);

//        drawing each drop sprite
        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createDroplet() {
//        each droplet will be 1x1 unit on the viewport window
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

//        let's create droplet sprite
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }
}
