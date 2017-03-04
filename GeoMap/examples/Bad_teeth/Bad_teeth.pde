import org.gicentre.geomap.*;

//  Draws a choropleth map of dental health data from gapminder.org.

GeoMap geoMap;
Table tabBadTeeth;
color minColour, maxColour;
float dataMax;

void setup()
{
  size(820, 440);
  displayDensity(pixelDensity);
 
  // Read map data.
  geoMap = new GeoMap(10, 10, width-20, height-40, this);
  geoMap.readFile("world");
  geoMap.writeAttributesAsTable(5);         // Display first 5 rows of attribute table in console for checking.
  
  tabBadTeeth = loadTable("badTeeth.csv");  // Read dental health data.

  // Find largest data value so we can scale colours.
  dataMax = 0;
  for (TableRow row : tabBadTeeth.rows())
  {
    dataMax = max(dataMax, row.getFloat(2));
  }

  minColour = color(222, 235, 247);   // Light blue
  maxColour = color(49, 130, 189);    // Dark blue.
}

void draw()
{
  background(255);
  stroke(255);
  strokeWeight(0.5);

  // Draw countries
  for (int id : geoMap.getFeatures().keySet())
  {
    String countryCode = geoMap.getAttributeTable().findRow(str(id),0).getString("ISO_A3");    
    TableRow dataRow = tabBadTeeth.findRow(countryCode, 1);

    if (dataRow != null)       // Table row matches country code
    {
      float normBadTeeth = dataRow.getFloat(2)/dataMax;
      fill(lerpColor(minColour, maxColour, normBadTeeth));
    }
    else                   // No data found in table.
    {
      fill(250);
    }
    geoMap.draw(id); // Draw country
  }

  // Draw title text
  fill(50);
  textAlign(LEFT, TOP);
  text("Number of bad teeth per 12 year-old child", 10, height-20);

  noLoop();    // Static map so no need to redraw.
}