package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;

public abstract class AbstractNumberTsonElement extends AbstractPrimitiveTsonElement implements TsonNumber{
    private TsonNumberLayout layout;
    private String unit;
    public AbstractNumberTsonElement(TsonElementType type,TsonNumberLayout layout,String unit) {
        super(type);
        this.layout=layout==null?TsonNumberLayout.DECIMAL : layout;
        if(unit!=null){
            unit=unit.trim();
        }
        this.unit=unit==null?null:unit.isEmpty()?null:unit;
        if(this.unit!=null){
            if (this.layout==TsonNumberLayout.HEXADECIMAL){
                boolean someNonHex=false;
                for (char c : this.unit.toCharArray()) {
                    boolean isHex= ((c>='0' && c<='9')
                            || (c>='a' && c<='f')
                            || (c>='A' && c<='F')
                    );
                    if(!isHex){
                        someNonHex=true;
                        break;
                    }
                }
                if(!someNonHex){
                    throw new IllegalArgumentException("Unit '"+this.unit+"' is hexadecimal and hence is unacceptable");
                }
            }
        }
    }

    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(String.valueOf(numberValue()));
    }


    @Override
    public TsonNumber toNumber() {
        return this;
    }

    public TsonNumberLayout numberLayout() {
        return layout;
    }

    @Override
    public String numberSuffix() {
        return unit;
    }
}
