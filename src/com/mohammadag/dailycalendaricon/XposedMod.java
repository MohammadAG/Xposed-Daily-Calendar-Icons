package com.mohammadag.dailycalendaricon;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XposedMod implements IXposedHookZygoteInit {
	private static final String GOOGLE_CALENDAR_PKG = "com.google.android.calendar";
	private static final String GOOGLE_CALENDAR_LAUNCHER_ACTIVITY = "com.android.calendar.AllInOneActivity";
	private static final int FAKE_INT = 999122;
	private XModuleResources mResources;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mResources = XModuleResources.createInstance(startupParam.modulePath, null);
		XposedHelpers.findAndHookMethod(ComponentInfo.class, "getIconResource", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (!(param.thisObject instanceof ActivityInfo))
					return;

				ActivityInfo info = (ActivityInfo) param.thisObject;				
				if (!GOOGLE_CALENDAR_PKG.equals(info.packageName))
					return;

				if (GOOGLE_CALENDAR_LAUNCHER_ACTIVITY.equals(info.name)) {
					boolean shouldReplace = true;
					StackTraceElement[] elems = Thread.currentThread().getStackTrace();
					for (StackTraceElement elem : elems) {
						String className = elem.getClassName();
						if (className == null)
							continue;

						if (GOOGLE_CALENDAR_LAUNCHER_ACTIVITY.equals(className)) {
							shouldReplace = false;
							break;
						}
					}
					if (shouldReplace)
						param.setResult(FAKE_INT);
				}
			}
		});

		XposedHelpers.findAndHookMethod(Resources.class, "getDrawableForDensity", int.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Resources res = (Resources) param.thisObject;
				int id = (Integer) param.args[0];
				if (id == FAKE_INT) {
					Date date = Calendar.getInstance().getTime();
					SimpleDateFormat format = new SimpleDateFormat("dd", Locale.getDefault());
					String formattedDate = format.format(date);

					Bitmap bm = BitmapFactory.decodeResource(mResources,
							R.drawable.ic_calendar).copy(Bitmap.Config.ARGB_8888, true);
					param.setResult(writeOnBmp(res, bm, formattedDate));
				}		
			}
		});
	}

	public static BitmapDrawable writeOnBmp(Resources res, Bitmap bm, String text){
		Paint paint = new Paint(); 
		paint.setStyle(Style.FILL);  
		paint.setColor(Color.parseColor("#3b83f5")); 
		paint.setTextSize(42); 
		paint.setTextAlign(Align.CENTER);
		paint.setAntiAlias(true);

		Canvas canvas = new Canvas(bm);
		canvas.drawText(text, bm.getWidth()/2, (bm.getHeight()*2/3)-5, paint);

		return new BitmapDrawable(res, bm);
	}
}
