package com.slightsite.app.ui.component;

import android.support.v4.app.Fragment;

/**
 * Fragment which is able to call update() from other class.
 * This is used by Delegation pattern.
 *
 *
 */
public abstract class UpdatableFragment extends Fragment {

	/**
	 * Update fragment.
	 */
	public abstract void update();

}
