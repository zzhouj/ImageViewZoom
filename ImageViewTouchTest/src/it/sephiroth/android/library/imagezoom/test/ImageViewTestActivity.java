package it.sephiroth.android.library.imagezoom.test;

import it.sephiroth.android.library.imagezoom.test.utils.DecodeUtils;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class ImageViewTestActivity extends Activity {

	ImageViewFlow mImage;
	Button mButton;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView( R.layout.main );
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mImage = (ImageViewFlow) findViewById( R.id.image );
		mButton = (Button) findViewById( R.id.button );
		
		mButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick( View v ) {
				selectRandomImage();
			}
		} );
	}

	public void selectRandomImage() {
		Cursor c = getContentResolver().query( Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null );
		if ( c != null ) {
			int count = c.getCount();
			int position = (int) ( Math.random() * count );
			if ( c.moveToPosition( position ) ) {
				long id = c.getLong( c.getColumnIndex( Images.Media._ID ) );

				Uri imageUri = Uri.parse( Images.Media.EXTERNAL_CONTENT_URI + "/" + id );
				Bitmap bitmap = DecodeUtils.decode( this, imageUri, 1280, 1280 );
				if( null != bitmap )
				{
					//mImage.setMinZoom( 1.5f ); // you can set the minimum zoom of the image ( must be called before anything else )
					//mImage.setFitToScreen( true ); // calling this will force the image to fit the ImageView container width/height
					//mImage.setImageBitmap( bitmap, true, null, 5.0f );
					mImage.setImageBitmap(bitmap);
					mImage.setFlowScrollTime(60 * 1000);
				} else {
					Toast.makeText( this, "Failed to load the image", Toast.LENGTH_LONG ).show();
				}
			}
			c.close();
			c = null;
			return;
		}
	}
}
