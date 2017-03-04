import org.gicentre.geomap.*;

// Simple interactive world map that highlights selected countries.
GeoMap geoMap;

void setup()
{
  size(800, 400);

  geoMap = new GeoMap(this);  // Create the geoMap object.
  geoMap.readFile("world");   // Read shapefile.
}

void draw()
{
  background(202, 226, 245);  // Ocean colour
  stroke(0, 40);              // Boundary colour

  // Draw entire world map.
  fill(206, 173, 146);        // Land colour
  geoMap.draw();              // Draw the entire map.

  // Find the country at the mouse position and draw it in different colour.
  int id = geoMap.getID(mouseX, mouseY);
  if (id != -1)
  {
    fill(180, 120, 120);      // Highlighted land colour.
    geoMap.draw(id);
  }
}