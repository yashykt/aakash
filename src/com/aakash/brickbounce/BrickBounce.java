package com.aakash.brickbounce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

@SuppressLint("HandlerLeak")
public class BrickBounce extends View{

	private final class HandlerExtension extends Handler {
		public void handleMessage(Message msg)
		{
			invalidate();
		}
	}

	Paint rpaint;
	Rect up, left, right, down;
	int leftchange, templeft, prevleft, topchange, temptop, prevtop, ballrad, score, leftbound, rightbound, upbound, downbound;
	double dx, dy, ballx, bally, slope, angle;
	Display display;
	Thread t;
	static Handler handler;
	boolean over;
	DisplayMetrics metrics;
	int padlength, padwidth, dpheight, dpwidth;
	float density;
	int height, width;
	MediaPlayer bounce, crash;
	BrickBounceActivity parent;
	
	public BrickBounce(Context context)
	{
		super(context);
	}
	
	public BrickBounce(BrickBounceActivity obj) {
		super(obj.getApplicationContext());
		
		// TODO Auto-generated constructor stub
		WindowManager wm = (WindowManager) obj.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		parent = obj;
		display = wm.getDefaultDisplay();
		metrics = getResources().getDisplayMetrics();
		density = metrics.density;
		height = metrics.heightPixels;
		width = metrics.widthPixels;
		rpaint = new Paint();
		padlength = (height + width)/10;
		padwidth = padlength/10;
		rpaint.setTextSize(20*density);
		
		bounce = MediaPlayer.create(getContext(), R.raw.bounce);
		crash = MediaPlayer.create(getContext(), R.raw.crash);
		init();
	}

	private void init()
	{	
		leftbound = 2 * padwidth;
		rightbound = width - width / 10;
		upbound = 3 * padwidth;
		downbound = height - height / 12;
		
		down = new Rect(leftbound, downbound, leftbound + padlength, downbound + padwidth);
		up = new Rect(leftbound, upbound, leftbound + padlength, upbound + padwidth);
		left = new Rect(leftbound, upbound, leftbound + padwidth, upbound + padlength);
		right = new Rect(rightbound , upbound, rightbound + padwidth, upbound + padlength);
		
		
		ballx = width/2;
		bally = height/2;
		ballrad = padwidth;
		angle = 225;
		score = 0;
		over = true;
		
		newcoord();
		
		t = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				float time = 4 / density;
				int timemili = (int)(Math.floor(time));
				int timenano = (int)(time - Math.floor(time)) * 1000000;
				try
				{
					while(over = gameover())
					{
						ballx += dx;
						bally += dy;
						score++;
						timenano -= 20 * density;
						if(timenano < 0)
						{
							if(timemili > 0)
							{
								timemili -= 1;
								timenano = 999999;
							}
							else
								timenano = 0;
						}
						Log.d("time", Integer.toString(timemili) + " " + Integer.toString(timenano));
						
						if(hitpad())
							bounce.start();

						handler.sendEmptyMessage(0);
						Thread.sleep(timemili, timenano);
					}
					crash.start();					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
			}
		});
		handler = new HandlerExtension();
		t.start();

	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	
		if(over)
		{
			canvas.drawCircle((int)ballx, (int)bally, ballrad, rpaint);
			canvas.drawRect(up, rpaint);
			canvas.drawRect(down, rpaint);
			canvas.drawRect(left, rpaint);
			canvas.drawRect(right, rpaint);
		}
		else
		{
			canvas.drawText("Game Over", width/10, height/10, rpaint);
			canvas.drawText("Score : " + Integer.toString(score), width/10, height/10 + padlength/2, rpaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				if(over == false)
				{
			
					Intent intent = new Intent(parent.getApplicationContext(), MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					parent.startActivity(intent);
					parent.finish();
					return true;
				}
				leftchange = x;
				topchange = y;
				prevleft = down.left;
				prevtop = left.top;
				break;
					
			case MotionEvent.ACTION_MOVE:
				templeft = prevleft + x - leftchange;
				temptop = prevtop + y - topchange;
				if(templeft >= leftbound && templeft <= (rightbound - padlength + padwidth))
				{
					up.left = down.left = templeft;
					up.right = down.right = down.left + padlength;
				}
				if(temptop >= upbound && temptop <= (downbound - padlength + padwidth))
				{
					left.top = right.top = temptop;
					left.bottom = right.bottom = left.top + padlength;
				}
		}
		
		return true;
	}

	public boolean hitpad()
	{
		boolean downtouch, uptouch, lefttouch, righttouch;
		downtouch = uptouch = lefttouch = righttouch = false;
		//ball touches down pad
		if((((int)bally + ballrad) == down.top) && (ballx >= down.left && ballx <= down.right))
			downtouch = true;
		
		//ball touches up pad
		if((((int)bally - ballrad) == up.bottom) && (ballx >= up.left && ballx <= up.right))
			uptouch = true;
		
		//ball touches left pad
		if((((int)ballx - ballrad) == left.right) && (bally >= left.top && bally <= left.bottom))
			lefttouch = true;
	
		//ball touches right pad
		if((((int)ballx + ballrad) == right.left) && (bally >= right.top && bally <= right.bottom))
			righttouch = true;


		if(lefttouch && uptouch)
			angle = 45;
		else if(righttouch && downtouch)
			angle = 225;
		else if(lefttouch && downtouch)
			angle = 315;
		else if(righttouch && uptouch)
			angle = 135;
		
		else if(lefttouch)
		{
			angle =  (bally - left.top) / 200 * 180;
			if(angle < 90)
				angle += 270;
			else
				angle -= 90; 
		}
		else if(righttouch)
			angle = 270 - (bally - right.top) / 200 * 180;
		else if(downtouch)
			angle =  180 + (ballx - down.left) / 200 * 180;
		else if(uptouch)
			angle =  180 - (ballx - up.left) / 200 * 180;
		else
			return false;
		
		newcoord();
		return true;
	}
	
	public void newcoord()
	{
		slope = Math.tan(Math.toRadians(angle));
		if((angle >= 0 && angle <= 45) || (angle > 315) && (angle <= 360))
		{
			dx = 1;
			dy = slope * dx;
		}
		else if(angle > 45 && angle <= 135)
		{
			dy = 1;
			dx = dy / slope;
		}
		else if(angle > 135 && angle <= 225)
		{
			dx = -1;
			dy = slope * dx;
		}
		else
		{
			dy = -1;
			dx = dy / slope;
		}

	}
	
	public boolean gameover()
	{
		if((ballx - ballrad) < left.left || (ballx + ballrad) > right.right || (bally - ballrad) < up.top || (bally + ballrad) > down.bottom)
			return false;
		return true;
	}

}
