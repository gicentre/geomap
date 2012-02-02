/*
  Draws a choropleth map of bad teeth data from gapminder.org.
  It uses the giCentre's geoMap library.
  Iain Dillingham, 2nd February 2012.
*/
import org.gicentre.geomap.*;

GeoMap geoMap;
Table tabBadTeeth;
color cbGreysC, cbGreysF, cbGreysI, cbBluesC, cbBluesI;
float dataMax, dataMin;

void setup()
{
  size(820, 440);
  smooth();
  noLoop();

  geoMap = new GeoMap(10, 10, width-20, height-40, this);
  geoMap.readFile("world");
  tabBadTeeth = new Table("bad_teeth.tsv", this);

  // Colours from http://colorbrewer.org/
  cbGreysC = color(240);
  cbGreysF = color(189);
  cbGreysI = color(99);
  cbBluesC = color(222, 235, 247);
  cbBluesI = color(49, 130, 189);

  // Maximum and minimum data values
  dataMax = -MAX_FLOAT;
  dataMin = +MAX_FLOAT;

  for (int row = 0; row < tabBadTeeth.getRowCount(); row++)
  {
    dataMax = max(dataMax, tabBadTeeth.getFloatAt(row, 1));
    dataMin = min(dataMin, tabBadTeeth.getFloatAt(row, 1));
  }

  // Fonts
  PFont font = createFont("Blokletters-Balpen", 10);
  textFont(font);
}

void draw()
{
  background(255);
  stroke(cbGreysF);
  strokeWeight(0.5);

  // Draw countries
  for (int id : geoMap.getFeatures().keySet())
  {
    String country = geoMap.getAttributes().getString(id, 3);
    String badTeeth = tabBadTeeth.getString(country, 1);

    if (badTeeth.equals("")) // No data
    {
      fill(cbGreysC);
    }
    else // Data
    {
      float normBadTeeth = norm(float(badTeeth), dataMin, dataMax);
      fill(lerpColor(cbBluesC, cbBluesI, normBadTeeth));
    }

    geoMap.draw(id); // Draw country
  }

  // Draw title text
  fill(cbGreysI);
  String title = "Number of bad teeth per 12 year-old child (gapminder.org)";
  textAlign(LEFT, CENTER);
  text(title, 10, height-30, width-20, 30);

  // Draw the frame line
  strokeWeight(1);
  noFill();
  rect(10, 10, width-20, height-40);
}

void keyPressed()
{
  // Save a copy of the map
  if (key == 'c')
  {
    save("Bad_teeth.png");
  }
}

