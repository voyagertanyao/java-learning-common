package com.cmcciot.platform.common.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.cmcciot.platform.common.bean.Item;

/**
 * <p>Title: xml解析</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2009</p>
 * @author administrator
 * @version 1.0
 */

public class XMLParser
{
    public Document document = null;

    public XMLParser( String path ) throws Exception
    {
        File file = new File( path );

        if( file.exists() )
        {
            buildDocument( new FileInputStream( file ) );
        }
        else
        {
            throw new Exception("Unknown xml file:"+file.getPath());
        }
    }

    public XMLParser( File file ) throws Exception
    {
        if( file.exists() )
        {
            buildDocument( new FileInputStream( file ) );
        }
        else
        {
            throw new Exception("Unknown xml file:"+file.getPath());
        }
    }

    public XMLParser( InputStream inputStream ) throws Exception
    {
        buildDocument( inputStream );
    }

    private Document buildDocument( InputStream inputStream ) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.document = builder.parse( inputStream );
        return document;
    }

    /**
     * 得到根节点
     * @return 根节点
     */
    public Node getRootNode()
    {
        if( document == null )
        {
            return null;
        }

        for( Node node = document.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                return node;
            }
        }
        return null;
    }
    
    /**
     * 根据名字获取根节点
     * @param tag
     * @return
     */
    public Node getRootTagByName(String tag)
    {
    	for( Node node = document.getFirstChild();
                node != null; node = node.getNextSibling() )
		{
		   if( node.getNodeType() == Node.ELEMENT_NODE 
				   && node.getNodeName().equalsIgnoreCase(tag))
		   {
		       return node;
		   }
		}
    	return null;
    }

    /**
     * 通过标签名称得到对应的节点(包括当前节点)
     * @param element
     * @param tag
     * @return
     */
    public static Node getElementByTag( Node element, String tag )
    {
        if( element.getNodeType() != Node.ELEMENT_NODE )
        {
            return null;
        }
        //判断节点名称是否和输入的标签名称相同,如果相同就返回该节点
        if( element.getNodeName().equalsIgnoreCase( tag ) )
        {
            return element;
        }

        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            Node e = getElementByTag( node, tag );
            if( e != null )
            {
                return e;
            }
        }
        return null;
    }

    public static Node getLastChildElement( Node element )
    {
        Node e = null;
        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                e = node;
            }
        }

        return e;
    }

    public static int countChildElement( Node element )
    {
        int i = 0;
        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                i += 1;
            }
        }

        return i;
    }
    /**
     * 返回第一个子节点元素
     * @param element Node
     * @return Node
     */
    public static Node getFirstChildElement( Node element )
    {
        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                return node;
            }
        }

        return null;
    }

    /**
     * 通过标签名称得到对应的节点(不包括包括当前节点)
     * @param element
     * @param tag
     * @return
     */
    public static Node getChildElementByTag( Node element, String tag )
    {
        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                //判断节点名称是否和输入的标签名称相同,如果相同就返回该节点
                if( node.getNodeName().equalsIgnoreCase( tag ) )
                {
                    return node;
                }
                Node e = getElementByTag( node, tag );
                if( e != null )
                {
                    return e;
                }
            }
        }

        return null;
    }

    public static final String getElementValue( Node element )
    {
        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.TEXT_NODE )
            {
                return node.getNodeValue();
            }
        }
        return "";
    }

    /**
     * 根据属性名称得到指定节点的属性值
     * @param element
     * @param id
     * @return
     */
    public static final String getElementAttr( Node element, String attrName )
    {
        NamedNodeMap map = element.getAttributes();
        int i = 0;
        for( Node n = map.item( i ); n != null; n = map.item( i ) )
        {
            if( n.getNodeType() == Node.ATTRIBUTE_NODE &&
                n.getNodeName().equalsIgnoreCase( attrName ) )
            {
                return n.getNodeValue();
            }
            i++;
        }
        return "";
    }

    public static final Item[] getElementAttrs( Node element )
    {
        ArrayList<Item> list = new ArrayList<Item>();
        NamedNodeMap map = element.getAttributes();
        int i = 0;
        for( Node n = map.item( i ); n != null; n = map.item( i ) )
        {
            if( n.getNodeType() == Node.ATTRIBUTE_NODE )
            {
                Item item = new Item( n.getNodeName(), n.getNodeValue() );
                list.add( item );
            }
            i++;
        }

        Item items[] = new Item[list.size()];
        list.toArray(items);
        return items;
    }

    public static final Node getNextSibling( Node element )
    {
        for( Node node = element.getNextSibling(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE )
            {
                return node;
            }
        }
        return null;
    }

    public static final Element nextSibling( Node e )
    {
        for( Node n = e.getNextSibling(); n != null; n = n.getNextSibling() )
        {
            if( n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
            {
                continue;
            }
            //如果得到的下一个节点名称和输入的节点名称相同那么就返回新的节点
            if( e.getNodeName().equalsIgnoreCase( n.getNodeName() ) )
            {
                return( Element ) n;
            }
        }
        return null;
    }

    /**
     * 返回所有节点名称（静态信息的那种结构）
     * @return
     */
    public String[] getAttributes()
    {

        Vector<String> temp = new Vector<String>();
        for( int i = 0; i < document.getFirstChild().getChildNodes().getLength();
                     i++ )
        {
            if( document.getFirstChild().getChildNodes().item( i ).getNodeType() ==
                Node.ELEMENT_NODE )
            {
                temp.add( document.getFirstChild().getChildNodes().item( i ).
                          getNodeName() );
            }
        }
        Object[] tempObject = temp.toArray();
        String[] attributes = new String[tempObject.length];
        for( int i = 0; i < tempObject.length; i++ )
        {
            attributes[i] = tempObject[i].toString();
        }
        return attributes;
    }

    /**
     * 返回所有节点值（静态信息的那种结构）
     * @return
     */
    public String[] getValues()
    {

        Vector<Node> temp = new Vector<Node>();
        for( int i = 0; i < document.getFirstChild().getChildNodes().getLength();
                     i++ )
        {
            if( document.getFirstChild().getChildNodes().item( i ).getNodeType() ==
                Node.ELEMENT_NODE )
            {
                temp.add( document.getFirstChild().getChildNodes().item( i ).
                          getFirstChild() );
            }
        }
        Object[] tempObject = temp.toArray();
        String[] values = new String[tempObject.length];
        for( int i = 0; i < tempObject.length; i++ )
        {
            values[i] = tempObject[i].toString();
        }
        return values;
    }

    /**    public static String getAttribute(Node element, String attrName)
        {
            NamedNodeMap map = element.getAttributes();
            map.
            for(
        }*/
    public static void main( String[] args )
    {
//        XMLParser handle = new XMLParser(
        //      "E:/hfc/src/resources/config/testdata/静态信息.xml");
        //    if(handle.document != null )
        //  {
        //    System.out.println( "解析XML文档成功!" );
        /*            System.out.println( handle.document.getChildNodes().item(0).getNodeType() );
         System.out.println( handle.document.getChildNodes().getLength() );
                    System.out.println( handle.getChildElementByTag(
                        handle.getRootNode(),"input-params"));
                    System.out.println( handle.getRootNode())
                    System.out.println(handle.getChildElementByTag(handle.getRootNode(),"menubar").getAttributes().getLength());
             NamedNodeMap map = handle.getChildElementByTag(handle.getRootNode(),"menubar").getAttributes();
                    System.out.println(map.item(0).getNodeValue());;*/
        //  System.out.println(handle.getAttributes().length);
//	    System.out.println(handle.getValues().length);
        //      }
        //    else
        //  {
        //    System.out.println( "解析XML文档失败!" );
//        }
    }

    public String toString()
    {
        return this.getRootNode().toString();
    }

    /**
     * 创建解析好的XML的document对象
     * @param filename XML文件的文件名
     * @return 解析好的XML对象
     */
    public static final XMLParser createXMLParser( String path )
    {
        try
        {
            return new XMLParser( path );
        }
        catch( Exception eee )
        {
            return null;
        }
    }

    public static final XMLParser createXMLParser( byte[] buffer )
    {
        try
        {
            return new XMLParser( new ByteArrayInputStream( buffer ) );
        }
        catch( Exception eee )
        {
            return null;
        }
    }

    /**
     * 根据属性名获取第一个element(包含本身)
     * @param element
     * @param attrName
     * @param attrValue
     * @return
     */
    public static final Node getElementByAttr(Node element,String attrName,String attrValue)
    {
    	if(attrValue.equals(getElementAttr(element,attrName)))
    	{
    		return element;
    	}
    	for( Node node = element.getFirstChild();
		        node != null; node = node.getNextSibling() )
		{
			if( node.getNodeType() == Node.ELEMENT_NODE && attrValue.equals(getElementAttr(node,attrName)))
			{
				return node;
			}
		}
    	return null;
    }
    
}
