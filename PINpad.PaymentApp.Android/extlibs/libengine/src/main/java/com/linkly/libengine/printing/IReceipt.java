package com.linkly.libengine.printing;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libmal.IMal;
import com.linkly.libmal.global.printing.PrintReceipt;

import java.util.ArrayList;

public interface IReceipt {


    PrintReceipt generateReceipt(Object obj);

    PrintReceipt generateReceipt(Object obj, ArrayList<String> keys, Boolean isWhitelist, String configTitle);

    boolean isDuplicate();

    void setIsDuplicate(boolean b);

    boolean isMerchantCopy();

    void setIsMerchantCopy(boolean b);

    boolean isCardHolderCopy();

    void setIsCardHolderCopy(boolean b);

    void setDependencies(IDependency d, IMal mal);

    void setIsSAFReversalDelete(boolean b);


}
