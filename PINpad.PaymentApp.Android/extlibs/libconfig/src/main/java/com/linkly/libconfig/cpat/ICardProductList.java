package com.linkly.libconfig.cpat;

/**
 * Class to conform different types of cardProduct files
 * */
public interface ICardProductList {
    /**
     * Can be null if implementation is incomplete or throws an internal error
     * @return {@link CardProductList} object based on the implementation
     * */
    CardProductList getConfig();
}
