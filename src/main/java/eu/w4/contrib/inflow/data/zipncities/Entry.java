package eu.w4.contrib.inflow.data.zipncities;

import java.util.HashMap;
import java.util.Map;

public class Entry
{

  private String _insee;
  private String _zip;
  private String _name;

  public String getInsee()
  {
    return _insee;
  }

  public String getName()
  {
    return _name;
  }

  public String getZip()
  {
    return _zip;
  }

  public void setInsee(String _insee)
  {
    this._insee = _insee;
  }

  public void setName(String _name)
  {
    this._name = _name;
  }

  public void setZip(String _zip)
  {
    this._zip = _zip;
  }

  public Map<String, Object> toMap()
  {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("insee", _insee);
    map.put("zip", _zip);
    map.put("name", _name);
    return map;
  }
}
