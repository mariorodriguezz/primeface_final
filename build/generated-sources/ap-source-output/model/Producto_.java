package model;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import model.Venta;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-06-03T22:51:31")
@StaticMetamodel(Producto.class)
public class Producto_ { 

    public static volatile SingularAttribute<Producto, BigDecimal> precio;
    public static volatile CollectionAttribute<Producto, Venta> ventaCollection;
    public static volatile SingularAttribute<Producto, Long> idProducto;
    public static volatile SingularAttribute<Producto, String> descripcion;
    public static volatile SingularAttribute<Producto, Short> disponible;

}