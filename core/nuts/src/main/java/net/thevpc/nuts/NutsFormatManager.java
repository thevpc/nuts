package net.thevpc.nuts;

public interface NutsFormatManager {

    /**
     * create string format instance
     *
     * @return string format
     * @since 0.5.5
     */
    NutsStringFormat str();

    /**
     * create json format instance
     *
     * @return json format
     * @since 0.5.5
     */
    NutsJsonFormat json();

    /**
     * create xml format instance
     *
     * @return xml format
     * @since 0.5.5
     */
    NutsXmlFormat xml();

    /**
     * create element format instance
     *
     * @return element format
     * @since 0.5.5
     */
    NutsElementFormat element();

    /**
     * create tree format instance
     *
     * @return tree format
     * @since 0.5.5
     */
    NutsTreeFormat tree();

    /**
     * create table format instance
     *
     * @return json table
     * @since 0.5.5
     */
    NutsTableFormat table();

    /**
     * create properties format instance
     *
     * @return properties format
     * @since 0.5.5
     */
    NutsPropertiesFormat props();

    /**
     * create object format instance
     *
     * @return object format
     * @since 0.5.5
     */
    NutsObjectFormat object();

    /**
     * create iterable format instance
     *
     * @return iterable format
     * @since 0.5.6
     */
    NutsIterableOutput iter();

}
