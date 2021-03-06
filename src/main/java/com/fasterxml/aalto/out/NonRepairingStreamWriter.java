/* Aalto XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fasterxml.aalto.out;

import javax.xml.namespace.QName;
import javax.xml.stream.*;

import org.codehaus.stax2.ri.typed.AsciiValueEncoder;

import com.fasterxml.aalto.impl.ErrorConsts;

/**
 * Concrete implementation of {@link StreamWriterBase}, which
 * implements basic namespace-aware, non repairing functionality.
 */
public final class NonRepairingStreamWriter
    extends StreamWriterBase
{
    /*
    /**********************************************************************
    /* Construction, init
    /**********************************************************************
     */

    public NonRepairingStreamWriter(WriterConfig cfg, XmlWriter writer,
                                    WNameTable symbols)
    {
        super(cfg, writer, symbols);
    }

    /*
    /**********************************************************************
    /* Implementations of abstract methods from base class, Stax 1.0 methods
    /**********************************************************************
     */

    @Override
    public void setDefaultNamespace(String uri)
        throws XMLStreamException
    {
        _currElem.setDefaultNsURI(uri);
    }

    @Override
    public void _setPrefix(String prefix, String uri)
    {
        _currElem.addPrefix(prefix, uri);
    }

    //public void writeAttribute(String localName, String value)
    
    @Override
    public void writeAttribute(String nsURI, String localName, String value)
        throws XMLStreamException
    {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        WName name;
        if (nsURI == null || nsURI.length() == 0) {
            name = _symbols.findSymbol(localName);
        } else {
            String prefix = _currElem.getExplicitPrefix(nsURI, _rootNsContext);
            if (prefix == null) {
                throwOutputError("Unbound namespace URI '"+nsURI+"'");
            }
            name = _symbols.findSymbol(prefix, localName);
        }
        _writeAttribute(name, value);
    }

    @Override
    public void writeAttribute(String prefix, String nsURI,
                               String localName, String value)
        throws XMLStreamException
    {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        WName name = (prefix == null || prefix.length() == 0)
            ? _symbols.findSymbol(localName)
            : _symbols.findSymbol(prefix, localName);
        _writeAttribute(name, value);
    }

    @Override
    public void writeDefaultNamespace(String nsURI)
        throws XMLStreamException
    {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_NS_NO_ELEM);
        }
        _writeDefaultNamespace(nsURI);
        /* 31-Jan-2008, tatus: Stax TCK expects an implicit prefix
         *  addition binding. So let's do that, then
         */
        setDefaultNamespace(nsURI);
    }

    //public void writeDTD(String dtd)

    //public void writeEmptyElement(String localName)

    @Override
    public void writeEmptyElement(String nsURI, String localName)
        throws XMLStreamException
    {
        String prefix = _currElem.getPrefix(nsURI);
        if (prefix == null) {
            throwOutputError("Unbound namespace URI '"+nsURI+"'");
        }
        WName name;
        if (prefix.length() == 0) {
            name = _symbols.findSymbol(localName);
            prefix = null;
        } else {
            name = _symbols.findSymbol(prefix, localName);
        }
        _verifyStartElement(prefix, localName);
        _writeStartTag(name, true, nsURI);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String nsURI)
        throws XMLStreamException
    {
        _verifyStartElement(prefix, localName);
        WName name;
        if (prefix == null || prefix.length() == 0) {
            name = _symbols.findSymbol(localName);
        } else {
            name = _symbols.findSymbol(prefix, localName);
        }
        _writeStartTag(name, true, nsURI);
    }

    @Override
    public void writeNamespace(String prefix, String nsURI)
        throws XMLStreamException
    {
        if (prefix == null || prefix.length() == 0) {
            writeDefaultNamespace(nsURI);
            return;
        }
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_NS_NO_ELEM);
        }
        _writeNamespace(prefix, nsURI);
        // 31-Jan-2008, tatus: Stax TCK expects an implicit prefix
        //  addition binding. So let's do that, then
        setPrefix(prefix, nsURI);
    }

    //public void writeStartElement(String localName)

    @Override
    public void writeStartElement(String nsURI, String localName)
        throws XMLStreamException
    {
        String prefix = _currElem.getPrefix(nsURI);
        if (prefix == null) {
            throwOutputError("Unbound namespace URI '"+nsURI+"'");
        }
        WName name;
        if (prefix.length() == 0) {
            name = _symbols.findSymbol(localName);
            prefix = null;
        } else {
            name = _symbols.findSymbol(prefix, localName);
        }
        _verifyStartElement(prefix, localName);
        _writeStartTag(name, false);
    }

    @Override
    public void writeStartElement(String prefix, String localName,
                                  String nsURI)
        throws XMLStreamException
    {
        _verifyStartElement(prefix, localName);
        WName name;
        if (prefix == null || prefix.length() == 0) {
            name = _symbols.findSymbol(localName);
        } else {
            name = _symbols.findSymbol(prefix, localName);
        }
        _writeStartTag(name, false, nsURI);
    }

    /*
    /**********************************************************************
    /* Implementations of abstract methods from base class, Stax2 Typed API Access (v3.0)
    /**********************************************************************
     */

    @Override
    public void writeTypedAttribute(String prefix, String nsURI, String localName,
                                    AsciiValueEncoder enc)
        throws XMLStreamException
    {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        WName name = (prefix == null || prefix.length() == 0)
            ? _symbols.findSymbol(localName)
            : _symbols.findSymbol(prefix, localName);
        _writeAttribute(name, enc);
    }

    @Override
    protected String _serializeQName(QName name)
    {
        String prefix = name.getPrefix();
        String local = name.getLocalPart();
        if (prefix == null || prefix.length() == 0) {
            return local;
        }
        // Not efficient... but should be ok
        return prefix + ":" + local;
    }
}
