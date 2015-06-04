package bean;

import model.Venta;
import bean.util.JsfUtil;
import bean.util.PaginationHelper;
import facade.ProductoFacade;
import facade.VentaFacade;
import java.io.File;
import java.io.IOException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import model.Conexion;
import model.Producto;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperRunManager;
import org.primefaces.context.RequestContext;

@ManagedBean(name = "ventaController")
@SessionScoped
public class VentaController implements Serializable {

    private Venta current;
    private DataModel items = null;
    @EJB
    private facade.VentaFacade ejbFacade;
    @EJB
    private facade.ProductoFacade ejbProductoFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public VentaController() {
    }

    public Venta getSelected() {
        if (current == null) {
            current = new Venta();
            selectedItemIndex = -1;
        }
        return current;
    }

    private VentaFacade getFacade() {
        return ejbFacade;
    }

    private ProductoFacade getEjbProductoFacade() {
        return ejbProductoFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "listado";
    }

    public String prepareView() {
        current = (Venta) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "ver";
    }

    public String prepareCreate() {
        current = new Venta();
        selectedItemIndex = -1;
        return "nuevo";
    }

    public String create() {
        try {
            if (cantidadDisponible()>-1) {
                getFacade().create(current);
                Short disponibles = Short.parseShort(cantidadDisponible()+"");
                Producto producto = current.getIdProducto();
                producto.setDisponible(disponibles);
                getEjbProductoFacade().edit(producto);
                generarReporte(current.getIdVenta());
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VentaCreated"));
                return destroyAndView();
            } else {
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CantidadNoDisponible"));
                return "nuevo";
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Venta) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "modificar";
    }

    public String update() {
        try {
            if (cantidadDisponible()>-1) {
                getFacade().edit(current);
                Short disponibles = Short.parseShort(cantidadDisponible()+"");
                Producto producto = current.getIdProducto();
                producto.setDisponible(disponibles);
                getEjbProductoFacade().edit(producto);
                current = new Venta();
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VentaUpdated"));
                return "listado";
            } else {
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("CantidadNoDisponible"));
                return "modificar";
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }
    
    public void prepareDestroy() {
        current = (Venta) getItems().getRowData();
        RequestContext.getCurrentInstance().execute("PF('dlgConfirmarWV').show();");
    }

    public void destroy() {
        performDestroy();
        recreatePagination();
        recreateModel();        
        RequestContext.getCurrentInstance().execute("PF('dlgConfirmarWV').hide();");
        current = new Venta();
    }

    public String destroyAndView() {
        current = new Venta();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "listado";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "listado";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("VentaDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "listado";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "listado";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass = Venta.class)
    public static class VentaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            VentaController controller = (VentaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "ventaController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Venta) {
                Venta o = (Venta) object;
                return getStringKey(o.getIdVenta());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Venta.class.getName());
            }
        }

    }
    
    private int cantidadDisponible () {
        int disponibles = Integer.parseInt(current.getIdProducto().getDisponible()+"");
        int solicitados = Integer.parseInt(current.getCantidad()+"");
        
        return disponibles - solicitados;
    }
    
    public void setTotal ( ) {
        if (current.getIdProducto()!=null && current.getCantidad()!=null) {
            Integer solicitados = Integer.parseInt(current.getCantidad()+"");
            Double valor = Double.parseDouble(current.getIdProducto().getPrecio()+"");

            Double total = valor * solicitados;

            current.setTotal(BigDecimal.valueOf(total));
        }
    }
    
    private void generarReporte(Long idVenta) {
        try {
            Conexion conexion = new Conexion();
            Connection connection = conexion.conectar();
            
            File jasper = new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reportes/factura.jasper"));
            
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("idVenta", idVenta);
            
            byte[] bytes = null;
            JasperPrint jasperPrint = null;
            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();

            bytes = JasperRunManager.runReportToPdf(jasper.getPath(), params, connection);
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.setContentLength(bytes.length);
            
            servletOutputStream.write(bytes, 0, bytes.length);
            servletOutputStream.flush();
            servletOutputStream.close();
            
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException | JRException | ClassNotFoundException | SQLException e) {
            
        }
    }

}
