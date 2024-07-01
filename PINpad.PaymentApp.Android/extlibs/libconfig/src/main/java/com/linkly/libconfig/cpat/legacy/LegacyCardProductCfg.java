package com.linkly.libconfig.cpat.legacy;

import com.linkly.libconfig.cpat.CardProductList;
import com.linkly.libconfig.cpat.ICardProductList;
import com.linkly.libmal.global.config.JSONParse;

import timber.log.Timber;

public class LegacyCardProductCfg implements ICardProductList {
    private final CardProductList cardProductList;

    public LegacyCardProductCfg( String fileName ){
        JSONParse j = new JSONParse();

        // read card product info from cardproduct.json
        this.cardProductList = ( CardProductList ) j.parse( fileName, CardProductList.class );
        if( this.cardProductList == null ) {
            Timber.e( "Error parsing card product file %s", fileName );
        } else if(this.cardProductList.cards != null ){
            Timber.i( "Parsed legacy card product file %s okay. Num entries = %d", fileName, this.cardProductList.cards.size() );
        } else {
            Timber.e( "Parsed card product file %s, but no cards parsed", fileName );
        }
    }


    @Override
    public CardProductList getConfig() {
        return this.cardProductList;
    }
}
