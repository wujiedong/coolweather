package cn.wujiedong.coolweather.util;

import java.util.Collection;
import java.util.Map;


public class CollectionUtil {


	  public static boolean checkNotEmpty(Collection coll)
	  {
	    if ((coll != null) && (coll.size() != 0)) {
	      return true;
	    }
	    return false;
	  }

	  public static boolean checkNotEmpty(Map map)
	  {
	    if ((map != null) && (map.size() != 0)) {
	      return true;
	    }
	    return false;
	  }

	  public static boolean checkEmpty(Collection list)
	  {
	    if ((list == null) || (list.size() == 0)) {
	      return true;
	    }
	    return false;
	  }

	  public static boolean checkEmpty(Map map)
	  {
	    if ((map == null) || (map.size() == 0)) {
	      return true;
	    }
	    return false;
	  }

}
