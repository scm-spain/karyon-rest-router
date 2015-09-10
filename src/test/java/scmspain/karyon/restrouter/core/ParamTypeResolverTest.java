package scmspain.karyon.restrouter.core;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(DataProviderRunner.class)
public class ParamTypeResolverTest<T> {

  ParamTypeResolver<T> parameterResolver;

  @Before
  public void setUp() {
    parameterResolver = new ParamTypeResolver<>();
  }

  @DataProvider
  public static Object[][] dataProviderAdd() {
    // @formatter:off
    return new Object[][] {
      { String.class, "lorem ipsum", String.class },
      { Boolean.class, "true", Boolean.class },
      { Long.class, "27", Long.class },
      { Integer.class, "42", Integer.class },
      { Short.class, "2", Short.class },
      { Float.class, "27.42", Float.class },
      { Double.class, "27.42", Double.class },

      { boolean.class, "true", Boolean.class  },
      { long.class, "27", Long.class },
      { int.class, "42", Integer.class },
      { short.class, "2", Short.class },
      { float.class, "27.42", Float.class },
      { double.class, "42.42", Double.class },
    };
    // @formatter:on
  }


  @Test
  @UseDataProvider("dataProviderAdd")
  public void itShouldResolveValueType(
    Class<T> type,
    String value,
    Class<T> expectedClass
  ) throws Exception {
    assertEquals(expectedClass, parameterResolver.resolveValueType(type, value).getClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void itShouldReturnsErrorIfCanNotResolveValueType() {
    Class anyUnsupportedClass = Byte.class;
    parameterResolver.resolveValueType(anyUnsupportedClass, "byte");
  }
}