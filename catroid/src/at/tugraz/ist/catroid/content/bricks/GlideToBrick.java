/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010  Catroid development team 
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.content.bricks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.content.Sprite;
import at.tugraz.ist.catroid.ui.dialogs.EditDoubleDialog;
import at.tugraz.ist.catroid.ui.dialogs.EditIntegerDialog;

public class GlideToBrick implements Brick, OnDismissListener {
	private static final long serialVersionUID = 1L;
	private int xDestination;
	private int yDestination;
	private int durationInMilliSeconds;
	private Sprite sprite;
	public static final transient int BRICK_BEHAVIOUR = Brick.NORMAL_BRICK | Brick.BACKGROUND_BRICK;
	public static final transient int BRICK_RESSOURCES = Brick.NO_RESOURCES;

	private transient View view;

	public GlideToBrick(Sprite sprite, int xDestination, int yDestination, int durationInMilliSeconds) {
		this.sprite = sprite;
		this.xDestination = xDestination;
		this.yDestination = yDestination;
		this.durationInMilliSeconds = durationInMilliSeconds;
	}

	public void execute() {
		long startTime = System.currentTimeMillis();
		int duration = durationInMilliSeconds;
		while (duration > 0) {
			long timeBeforeSleep = System.currentTimeMillis();
			int sleep = 33;
			while (System.currentTimeMillis() <= (timeBeforeSleep + sleep)) {
				if (sprite.isPaused) {
					sleep = (int) ((timeBeforeSleep + sleep) - System.currentTimeMillis());
					long milliSecondsBeforePause = System.currentTimeMillis();
					while (sprite.isPaused) {
						if (sprite.isFinished) {
							return;
						}
						Thread.yield();
					}
					timeBeforeSleep = System.currentTimeMillis();
					startTime += System.currentTimeMillis() - milliSecondsBeforePause;
				}

				Thread.yield();
			}
			long currentTime = System.currentTimeMillis();
			duration -= (int) (currentTime - startTime);
			updatePositions((int) (currentTime - startTime), duration);
			startTime = currentTime;
			sprite.setToDraw(true);
		}
		sprite.setXYPosition(xDestination, yDestination);
		sprite.setToDraw(true);
	}

	private void updatePositions(int timePassed, int duration) {
		int xPosition = sprite.getXPosition();
		int yPosition = sprite.getYPosition();

		xPosition += ((float) timePassed / duration) * (xDestination - xPosition);
		yPosition += ((float) timePassed / duration) * (yDestination - yPosition);

		sprite.setXYPosition(xPosition, yPosition);
	}

	public Sprite getSprite() {
		return this.sprite;
	}

	public int getDurationInMilliSeconds() {
		return durationInMilliSeconds;
	}

	public View getView(Context context, int brickId, BaseExpandableListAdapter adapter) {

		if (view == null) {
			view = View.inflate(context, R.layout.toolbox_brick_glide_to, null);
		}

		EditText editX = (EditText) view.findViewById(R.id.toolbox_brick_glide_to_x_edit_text);
		editX.setText(String.valueOf(xDestination));
		EditIntegerDialog dialogX = new EditIntegerDialog(context, editX, xDestination, true);
		dialogX.setOnDismissListener(this);
		dialogX.setOnCancelListener((OnCancelListener) context);
		editX.setOnClickListener(dialogX);

		EditText editY = (EditText) view.findViewById(R.id.toolbox_brick_glide_to_y_edit_text);
		editY.setText(String.valueOf(yDestination));
		EditIntegerDialog dialogY = new EditIntegerDialog(context, editY, yDestination, true);
		dialogY.setOnDismissListener(this);
		dialogY.setOnCancelListener((OnCancelListener) context);
		editY.setOnClickListener(dialogY);

		EditText editDuration = (EditText) view.findViewById(R.id.toolbox_brick_glide_to_duration_edit_text);
		editDuration.setText(String.valueOf(durationInMilliSeconds / 1000.0));
		EditDoubleDialog dialogDuration = new EditDoubleDialog(context, editDuration, durationInMilliSeconds / 1000.0);
		dialogDuration.setOnDismissListener(this);
		dialogDuration.setOnCancelListener((OnCancelListener) context);
		editDuration.setOnClickListener(dialogDuration);

		return view;
	}

	public View getPrototypeView(Context context) {
		return View.inflate(context, R.layout.toolbox_brick_glide_to, null);
	}

	@Override
	public Brick clone() {
		return new GlideToBrick(getSprite(), xDestination, yDestination, getDurationInMilliSeconds());
	}

	public void onDismiss(DialogInterface dialog) {
		if (dialog instanceof EditIntegerDialog) {
			EditIntegerDialog inputDialog = (EditIntegerDialog) dialog;
			if (inputDialog.getRefernecedEditTextId() == R.id.toolbox_brick_glide_to_x_edit_text) {
				xDestination = inputDialog.getValue();
			} else if (inputDialog.getRefernecedEditTextId() == R.id.toolbox_brick_glide_to_y_edit_text) {
				yDestination = inputDialog.getValue();
			} else {
				throw new RuntimeException("Received illegal id from EditText: "
						+ inputDialog.getRefernecedEditTextId());
			}
		} else if (dialog instanceof EditDoubleDialog) {
			durationInMilliSeconds = (int) Math.round(((EditDoubleDialog) dialog).getValue() * 1000);
		}
		dialog.cancel();
	}
}
