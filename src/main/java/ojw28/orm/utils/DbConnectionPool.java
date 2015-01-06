/*******************************************************************************
 * Copyright 2014 Digital Technology Group, Computer Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package ojw28.orm.utils;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * A pool of connections to the openroommap database. Connections are
 * not in auto-commit mode by default, so it is necessary to peform a
 * commit or rollback before returning the connection to the pool.
 * @author ojw28
 */
public class DbConnectionPool {

    private static Logger mLogger = Logger.getLogger("ojw28.orm.servlet.DbConnectionPool");
	private static DbConnectionPool mSingleton = null;

	private GenericObjectPool mPool;
	private int mAccessCount = 0;
	
	static
	{
		try {
			mSingleton = new DbConnectionPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private DbConnectionPool() throws Exception
	{
		createPool();
	}
	
	public static DbConnectionPool getSingleton()
	{
		return mSingleton;
	}

	public Connection getConnection() throws SQLException
	{
		mAccessCount++;
		if((mAccessCount % 100) == 0)
		{
			mLogger.fine("Active\t"+mPool.getNumActive()+"\tIdle\t"+mPool.getNumIdle());
		}
		return java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:openroommap");
	}
		
	private void createPool() throws Exception
	{
		mPool = new GenericObjectPool();

		Properties props = new Properties();
		props.setProperty("user", "orm");
		props.setProperty("password", "openroommap");
		ConnectionFactory cf =
			new DriverConnectionFactory(new org.postgresql.Driver(),
					"jdbc:postgresql://localhost:5432/openroommap",
					props);

		KeyedObjectPoolFactory kopf = new GenericKeyedObjectPoolFactory(null, 10);

		new PoolableConnectionFactory(cf,mPool,kopf,null,false,false);

		for(int i = 0; i < 5; i++) {
			mPool.addObject();
		}

		// PoolingDataSource pds = new PoolingDataSource(gPool);
		PoolingDriver pd = new PoolingDriver();
		pd.registerPool("openroommap", mPool);

		for(int i = 0; i < 5; i++) {
			mPool.addObject();
		}
		
		mLogger.info("Created connection pool");
		mLogger.info("Active\t"+mPool.getNumActive()+"\tIdle\t"+mPool.getNumIdle());
	}
}
