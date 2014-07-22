/*
 * <!--
 *    Copyright (C) 2014 The NamelessRom Project
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */

package android.hardware;

import android.os.FileObserver;

/**
 * Smart Cover HAL
 *
 * For devices which have a sysfs interface for watching smart cover changes
 *
 */
public class SmartCoverHW {

    private static final String SMART_COVER_PATH = "";

    /** The mask is usually specified at include/linux/input.h */
    public static final int SW_MASK = 0x00;
    public static final int SW_BIT = 1 << SW_MASK;

    public static final int LID_OPEN = 0;

    /**
     * All HAF classes should export this boolean.
     * Real implementations must, of course, return true
     */
    public static boolean isSupported() { return false; }

    /**
     * @return Whether a file observer should be used to
     *         detect smartcover changes
     */
    public static boolean isMonitorable() { return false; }

    /**
     * @return Wheter we support detection via mask and / or bit
     */
    public static boolean isMaskable() { return false; }

    /**
     * @return Wheter the lid is closed
     */
    public static boolean isLidClosed(final int state) {
        return (state > LID_OPEN);
    }

    /**
     * @return Wheter the lid is open
     */
    public static boolean isLidOpen(final int state) {
        return (state == LID_OPEN);
    }

    /**
     * @return The path to the sysfs file
     */
    public static String getPath() { return SMART_COVER_PATH; }

    /**
     * @return Whether the Smart Cover is open
     */
    public static boolean isPathOpen() { return false; }
}