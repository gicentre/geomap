import org.gicentre.geomap.*;

// Simple interactive world map that queries the attributes
// and highlights selected countries.

GeoMap geoMap;

void setup()
{
  size(800, 400);

  geoMap = new GeoMap(this);
  geoMap.readFile("world");

  // Set up text appearance.
  textAlign(LEFT, BOTTOM);
  textSize(18);

  // Display the first 5 rows of attributes in the console.
  geoMap.writeAttributesAsTable(5);
}

void draw()
{
  background(202, 226, 245);  // Ocean colour
  stroke(0, 40);              // Boundary colour

  // Draw entire world map.
  fill(206, 173, 146);        // Land colour
  geoMap.draw();              // Draw the entire map.

  // Query the country at the mouse position.
  int id = geoMap.getID(mouseX, mouseY);
  if (id != -1)
  {
    fill(180, 120, 120);
    geoMap.draw(id);

    String name = geoMap.getAttributeTable().findRow(str(id),0).getString("NAME");    
    fill(0);
    text(name, mouseX+5, mouseY-5);
  }
}