import org.gicentre.geomap.*;

// Simple world map using the geoMap library
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
  fill(206,173,146);          // Land colour
  stroke(0,40);               // Boundary colour
  
  geoMap.draw();              // Draw the entire map.
  
  noLoop();                   // Static map so no need to redraw.
}