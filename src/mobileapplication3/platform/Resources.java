// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import java.io.DataInputStream;
import java.io.InputStream;

public class Resources {
    /**
     * Searches for sequential resource files of the form: prefix + "1" + suffix, prefix + "2" + suffix, etc.
     * Aborts at the first missing or empty file.
     *
     * @return the number of valid files found
     */
    public static int countSequentialResources(String prefix, String suffix) {
        int count = 0;
        String path;

        while (isValidResource(path = prefix + (count + 1) + suffix)) {
            Logger.log("Found resource: " + path);
            count++;
        }

        return count;
    }

    /**
     * Checks if a resource exists and can be read.
     */
    public static boolean isValidResource(String path) {
        InputStream is = null;
        DataInputStream dis = null;
        try {
            is = Platform.getResource(path);
            if (is == null) {
                return false;
            }

            dis = new DataInputStream(is);

            dis.readBoolean();

            return true;
        } catch (Exception ex) {
            // No such file, no read access, or the file is empty
            return false;
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception ignored) { }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) { }
            }
        }
    }
}