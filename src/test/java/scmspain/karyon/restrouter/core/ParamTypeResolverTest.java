package scmspain.karyon.restrouter.core;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(DataProviderRunner.class)
public class ParamTypeResolverTest<T> {

  @DataProvider
  public static Object[][] dataProviderAdd() {
    // @formatter:off
    return new Object[][] {
        { String.class, "lorem ipsum" },
        { Boolean.class, "true" },
        { Long.class, "27" },
        { Integer.class, "42" },
        { Short.class, "2" },
        { Float.class, "27.42" },
        { Double.class, "42.42" }

    };
  }


  @Test
  @UseDataProvider("dataProviderAdd")
  public void testResolveValueType(Class<T> type, String value) throws Exception {

    ParamTypeResolver parameterResolver = new ParamTypeResolver<T>();
    assertEquals(type, parameterResolver.resolveValueType(type, value).getClass());
  }
}