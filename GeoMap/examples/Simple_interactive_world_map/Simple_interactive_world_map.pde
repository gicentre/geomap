/*
  Draws a simple interactive world map.
  It uses the giCentre's geoMap library.
  Iain Dillingham, 18th January 2011.
*/
import org.gicentre.geomap.*;

GeoMap geoMap;

void setup()
{
  size(800, 400);
  smooth();
  geoMap = new GeoMap(this);
  geoMap.readFile("world");
}

void draw()
{
  background(180, 210, 240);
  fill(150, 190, 150);
  geoMap.draw();

  int id = geoMap.getID(mouseX, mouseY);
  if (id != -1)
  {
    fill(180, 120, 120);
    geoMap.draw(id);
  }
}

