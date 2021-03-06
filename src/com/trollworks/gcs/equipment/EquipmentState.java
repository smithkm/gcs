/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.equipment;

import com.trollworks.gcs.feature.Feature;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;

/** The possible states for a piece of equipment. */
public enum EquipmentState {
	/**
	 * The state for a piece of equipment that is being carried and should also have any of its
	 * {@link Feature}s applied. For example, a magic ring that is being worn on a finger.
	 */
	EQUIPPED {
		@Override
		public String toShortName() {
			return "E"; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return EQUIPPED_TITLE;
		}
	},
	/**
	 * The state for a piece of equipment that is being carried, but should not have any of its
	 * {@link Feature}s applied. For example, a magic ring that is being stored in a pouch.
	 */
	CARRIED {
		@Override
		public String toShortName() {
			return "C"; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return CARRIED_TITLE;
		}
	},
	/** The state of a piece of equipment that is not being carried. */
	NOT_CARRIED {
		@Override
		public String toShortName() {
			return "-"; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return NOT_CARRIED_TITLE;
		}
	};

	@Localize("Equipped")
	static String	EQUIPPED_TITLE;
	@Localize("Carried")
	static String	CARRIED_TITLE;
	@Localize("Not Carried")
	static String	NOT_CARRIED_TITLE;

	static {
		Localization.initialize();
	}

	/** @return The short form of its description, typically a single character. */
	public abstract String toShortName();
}
