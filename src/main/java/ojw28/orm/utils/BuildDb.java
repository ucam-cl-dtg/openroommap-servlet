package ojw28.orm.utils;

import java.io.*;
import java.sql.*;
import javax.xml.parsers.*;
import ojw28.orm.*;
import org.xml.sax.*;
import org.w3c.dom.*;
//import sloc.map25d.*;

public class BuildDb {

	private static Connection getNewConnection() throws SQLException
	{		
		String lUrl = "jdbc:postgresql://localhost:5432/";
		String lDbName = "openroommap";
		String lDriver = "org.postgresql.Driver";
		String lUserName = "orm"; 
		String lPassword = "openroommap";

		try
		{
			Class.forName(lDriver).newInstance();
		} 
		catch(Exception lE)
		{
			lE.printStackTrace();
		}
		Connection lConn = DriverManager.getConnection(lUrl+lDbName,lUserName,lPassword);
		lConn.setAutoCommit(false);
		return lConn;
	}
	
	@SuppressWarnings("unused")
	private void createMapTables(Connection iConnection) throws SQLException
	{
		Statement lS = iConnection.createStatement();	
		try
		{
			//Create the table of rooms
			lS.executeUpdate("CREATE TABLE room_table (roomid INT NOT NULL,"+
					"name VARCHAR(255) UNIQUE,"+
					"accesslevel INT NOT NULL,"+
					"PRIMARY KEY(roomid))"
			);
			//Create a table which maps floor polygons to rooms
			lS.executeUpdate("CREATE TABLE roompoly_table (polyid INT NOT NULL UNIQUE,"+
					"roomid INT NOT NULL,"+
					"FOREIGN KEY (roomid) REFERENCES room_table(roomid))"
			);	
			//Create the table of floor polygons
			lS.executeUpdate("CREATE TABLE floorpoly_table (polyid INT NOT NULL,"+
					"vertexnum INT NOT NULL,"+
					"x FLOAT NOT NULL,"+
					"y FLOAT NOT NULL,"+
					"z FLOAT NOT NULL,"+
					"edgetarget INT,"+
					"FOREIGN KEY (polyid) REFERENCES roompoly_table(polyid),"+
					"PRIMARY KEY(polyid,vertexnum))"
			);
			//Create a table for submaps
			lS.executeUpdate("CREATE TABLE submap_table ("+
					"submapid INT NOT NULL UNIQUE,"+
					"name VARCHAR(255) NOT NULL,"+
					"PRIMARY KEY(submapid))"
			);
			//Create a table which defines the floor polygons belonging to each submap
			lS.executeUpdate("CREATE TABLE submappoly_table (submapid INT NOT NULL,"+
					"polyid INT NOT NULL,"+
					"FOREIGN KEY (submapid) REFERENCES submap_table(submapid),"+
					"FOREIGN KEY (polyid) REFERENCES roompoly_table(polyid),"+
					"PRIMARY KEY(submapid,polyid))"
			);

			lS.executeUpdate("CREATE TABLE occupants_table (" +
					"crsid VARCHAR(255) NOT NULL,"+
					"roomid INT NOT NULL,"+
					"FOREIGN KEY (roomid) REFERENCES room_table(roomid),"+
					"PRIMARY KEY(crsid,roomid))"
			);
			
			iConnection.commit();
		}
		catch(SQLException lE)
		{
			iConnection.rollback();
			throw(lE);
		}
		finally
		{
			lS.close();
		}	
	}

	@SuppressWarnings("unused")
	private void importMap(Connection iConnection, String iMapXml) throws Exception
	{

		Statement lS = iConnection.createStatement();
		try
		{
			Document document = XmlHelper.parse(new File(iMapXml));
			Map25D lMap = new Map25D();
			lMap.readFromXml(document.getDocumentElement());
			
			for(Room lRoom : lMap.getRooms())
			{
				String lRoomValues = lRoom.getUid()+",'"+lRoom.getName()+"',"+lRoom.getAccessLevel();
				lS.executeUpdate("INSERT INTO room_table VALUES("+lRoomValues+")");

				for(FloorPoly lPoly : lRoom.getFloorPolys())
				{
					//lS.executeUpdate("INSERT INTO roompoly_table VALUES("+lPoly.getUid()+","+lRoom.getUid()+")");
					String lPolyValues = "";
					for(int li = 0; li < lPoly.getVertices().length; li+=3)
					{
						if(lPoly.getConnection(li/3) != null)
						{
							lPolyValues = lPoly.getUid()+","+(li/3)+","+
							lPoly.getVertices()[li]+","+lPoly.getVertices()[li+1]+","+lPoly.getVertices()[li+2]+","+
							lPoly.getConnection(li/3).getUid();			
						}
						else
						{
							lPolyValues = lPoly.getUid()+","+(li/3)+","+
							lPoly.getVertices()[li]+","+lPoly.getVertices()[li+1]+","+lPoly.getVertices()[li+2]+","+
							"NULL";								
						}
						lS.executeUpdate("INSERT INTO floorpoly_table VALUES("+lPolyValues+")");
					}
				}
			}
			
			lS.executeUpdate("INSERT INTO submap_table VALUES('0','Ground')");
			lS.executeUpdate("INSERT INTO submap_table VALUES('1','First')");	
			lS.executeUpdate("INSERT INTO submap_table VALUES('2','Second')");	
			
			populateSubmaps(lMap, lS);

			iConnection.commit();
		}
		catch(SQLException lE)
		{
			iConnection.rollback();
			throw lE;
		}
		finally
		{
			lS.close(); 
		}
	}
	
	private void populateSubmaps(Map25D iMap, Statement iS) throws SQLException
	{
		for(Room lRoom : iMap.getRooms())
		{
			if(lRoom.getName().equals("SW_Landing"))
			{
			for(FloorPoly lPoly : lRoom.getFloorPolys())
			{
				if(lPoly.getBounds().getMinZ() <= 0 && lPoly.getBounds().getMinZ() > -1.76f)
				{
					iS.executeUpdate("INSERT INTO submappoly_table VALUES('2',"+lPoly.getUid()+")");		    		
				}
				if(lPoly.getBounds().getMinZ() < -1.76f && lPoly.getBounds().getMinZ() > -5.28f)
				{
					iS.executeUpdate("INSERT INTO submappoly_table VALUES('1',"+lPoly.getUid()+")");		    		
				}
				if(lPoly.getBounds().getMinZ() < -5.28f && lPoly.getBounds().getMinZ() >= -7.04f)
				{
					iS.executeUpdate("INSERT INTO submappoly_table VALUES('0',"+lPoly.getUid()+")");		    		
				}
			}
			}
		}
	}

	@SuppressWarnings("unused")
	private void createItemTables(Connection iConnection) throws SQLException
	{
		Statement lS = null;
		try
		{
			lS = iConnection.createStatement();

			lS.executeUpdate("CREATE TABLE item_definition_table (" +
					"def_id INT NOT NULL,"+
					"name VARCHAR(255) UNIQUE NOT NULL,"+
					"ordering INT NOT NULL,"+
					"category VARCHAR(255) NOT NULL,"+
					"image_file VARCHAR(255),"+
					"description TEXT," +
					"height FLOAT NOT NULL,"+
					"flipable BOOLEAN NOT NULL," +
					"field_label VARCHAR(255) NOT NULL," +
					"movable BOOLEAN NOT NULL DEFAULT TRUE," +
					"PRIMARY KEY(def_id))"
			);		

			lS.executeUpdate("CREATE SEQUENCE poly_id_seq");
			lS.executeUpdate("CREATE TABLE item_polygon_table (" +
					"item_def_id INT NOT NULL,"+
					"poly_id INT UNIQUE,"+
					"fill_colour INT NOT NULL," +
					"fill_alpha FLOAT NOT NULL," +
					"edge_colour INT NOT NULL,"+
					"edge_alpha FLOAT NOT NULL," +
					"PRIMARY KEY(item_def_id,poly_id)," +
					"FOREIGN KEY (item_def_id) REFERENCES item_definition_table(def_id))"
			);	

			lS.executeUpdate("CREATE TABLE item_polygon_vertex_table (" +
					"poly_id INT NOT NULL,"+
					"vertex_id INT NOT NULL," +
					"x FLOAT NOT NULL," +
					"y FLOAT NOT NULL," +
					"PRIMARY KEY(poly_id,vertex_id)," +
					"FOREIGN KEY (poly_id) REFERENCES item_polygon_table(poly_id))"
			);
			
			lS.executeUpdate("CREATE SEQUENCE item_id_seq");
			lS.executeUpdate("CREATE TABLE placed_item_table (" +
					"item_id INT NOT NULL,"+
					"item_def_id INT NOT NULL," +
					"last_update INT," +
					"PRIMARY KEY(item_id)," +
					"FOREIGN KEY (item_def_id) REFERENCES item_definition_table(def_id))"
			);

			lS.executeUpdate("CREATE SEQUENCE item_update_seq");
			lS.executeUpdate("CREATE TABLE placed_item_update_table (" +
					"update_id INT NOT NULL,"+
					"crsid VARCHAR(255) NOT NULL," +
					"timestamp TIMESTAMP DEFAULT now()," +
					"item_id INT NOT NULL,"+
					"x FLOAT NOT NULL," +
					"y FLOAT NOT NULL," +
					"theta INT NOT NULL," +
					"floor_id INT NOT NULL," +
					"flipped BOOLEAN NOT NULL," +
					"deleted BOOLEAN NOT NULL," +
					"label TEXT NOT NULL," +
					"PRIMARY KEY(update_id)," +
					"FOREIGN KEY(item_id) REFERENCES placed_item_table(item_id)," +
					"FOREIGN KEY(floor_id) REFERENCES submap_table(submapid))"
			);
			
			lS.executeUpdate("ALTER TABLE placed_item_table ADD FOREIGN KEY (last_update) REFERENCES placed_item_update_table(update_id)");
			
			iConnection.commit();
		}
		catch(SQLException lE)
		{
			iConnection.rollback();
			throw(lE);
		}
		finally
		{
			if(lS != null)
			{
				lS.close();
			}
		}	
	}
	
	private void populateItemTables(Connection iConnection, String iDefinitionFile) 
		throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, SQLException
	{
		Document document = XmlHelper.parse(new File(iDefinitionFile));
		
		NodeList lItemNodes = document.getDocumentElement().getElementsByTagName("ItemDef");
		for(int li = 0; li < lItemNodes.getLength(); li++)
		{
			ItemDef lItem = new ItemDef();
			lItem.readFromXml((Element) lItemNodes.item(li));
			insertItemDef(iConnection, lItem);
		}
	}
	
	private void insertItemDef(Connection iConnection, ItemDef iItem) throws SQLException
	{
		PreparedStatement lInsertItem = null;
		PreparedStatement lInsertVertex = null;
		PreparedStatement lInsertPoly = null;
		PreparedStatement lGetPolyId = iConnection.prepareStatement("SELECT nextval('poly_id_seq')");
		try
		{
			System.out.println(iItem.getItemDefId());
			lInsertItem = iConnection.prepareStatement("INSERT INTO " +
					"item_definition_table(def_id,name,category,image_file,description,height,flipable,movable,field_label,ordering) VALUES" +
					"(?,?,?,?,?,?,?,?,?)");
			lInsertItem.setInt(1, iItem.getItemDefId());
			lInsertItem.setString(2, iItem.getName());
			lInsertItem.setString(3, iItem.getCategory());		
			lInsertItem.setString(4, iItem.getImageFile());	
			lInsertItem.setString(5, iItem.getDescription());	
			lInsertItem.setFloat(6, iItem.getHeight());	
			lInsertItem.setBoolean(7, iItem.isFlipable());
			lInsertItem.setBoolean(8, iItem.isMovable());
			lInsertItem.setString(9, iItem.getFieldLabel());
			lInsertItem.setInt(10, iItem.getOrdering());
			
			lInsertItem.executeUpdate();
			System.out.println(lInsertItem.toString()+";");
			
			lInsertPoly = iConnection.prepareStatement("INSERT INTO " +
					"item_polygon_table(item_def_id,poly_id,fill_colour,fill_alpha,edge_colour,edge_alpha) VALUES" +
					"(?,?,?,?,?,?)");
			lInsertPoly.setInt(1, iItem.getItemDefId());
			
			lInsertVertex = iConnection.prepareStatement("INSERT INTO " +
					"item_polygon_vertex_table(poly_id,vertex_id,x,y) VALUES" +
					"(?,?,?,?)");
			
			for(int li = 0; li < iItem.getPolys().length; li++)
			{
				ResultSet lRes = lGetPolyId.executeQuery();
			    lRes.next();
			    int lPolyId = lRes.getInt(1);
			    lRes.close();
			    
				ItemDefPoly lPoly = iItem.getPolys()[li];
				lInsertPoly.setInt(2, lPolyId);
				lInsertPoly.setInt(3, lPoly.getFillColour());
				lInsertPoly.setFloat(4, lPoly.getFillAlpha());
				lInsertPoly.setInt(5, lPoly.getEdgeColour());
				lInsertPoly.setFloat(6, lPoly.getEdgeAlpha());
				
				lInsertPoly.executeUpdate();
				
				lInsertVertex.setInt(1, lPolyId);
			    for(int lj = 0; lj < lPoly.getVertices().length; lj+=2)
			    {
			    	lInsertVertex.setInt(2, lj/2);
			    	lInsertVertex.setFloat(3, lPoly.getVertices()[lj]);
			    	lInsertVertex.setFloat(4, lPoly.getVertices()[lj+1]);
			    	
			    	lInsertVertex.executeUpdate();
					System.out.println(lInsertVertex.toString()+";");
			    }
			}			
						
			iConnection.commit();
		}
		catch(SQLException lE)
		{
			iConnection.rollback();
			throw(lE);
		}
		finally
		{
			if(lInsertItem != null)
			{
				lInsertItem.close();
			}
			if(lInsertPoly != null)
			{
				lInsertPoly.close();
			}
			if(lInsertVertex != null)
			{
				lInsertVertex.close();
			}
			if(lGetPolyId != null)
			{
				lGetPolyId.close();
			}
		}	
	}
	
	public static void main(String[] args)
	{
		try
		{
			BuildDb lDb = new BuildDb();
			Connection lConnection = getNewConnection();
			//lDb.createMapTables(lConnection);
			//lDb.importMap(lConnection, "data/wgb.xml");
			//lDb.createItemTables(lConnection);
			lDb.populateItemTables(lConnection, "data/new.xml");
			
			lConnection.close();
		}
		catch(Exception lE)
		{
			lE.printStackTrace();
		}
	}

}
