/* ****************************************************************************************************************** *
 * DbReplicator.java                                                                                                  *
 * github.com/a-zz, 2018                                                                                              *
 * ****************************************************************************************************************** */

package io.github.azz.sql;

import java.util.UUID;

/**
 * DB replication support for clustered environments
 * @author a-zz
 */
public class DbReplicator {

	/**
	 * Gets a unique universal identifier for replicable objects (database table rows)
	 * @return
	 */
	public static String getUUID() {
		
		return UUID.randomUUID().toString();
	}
	
}
/* ****************************************************************************************************************** */