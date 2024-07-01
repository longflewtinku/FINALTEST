package com.linkly.libengine.action.Loyalty;

public interface GameCode {

    String WIN_GAME_PACKAGE_NAME = "com.winloyalty";
    String NOMINATE_GAME_PACKAGE_NAME = "com.nominateloyalty";

    String GAME_ACTION = "com.nominateloyalty.confirm";
    String OPEN_PLM_ACTION = "com.plm.OPEN_APP";

    int OPEN_GAME_REQUEST = 1010;

    String CARD_TOKEN = "card_token";
    String AMOUNT = "amount";
    String DISCOUNT_AMOUNT = "discount_amount";
    String IS_REWARD = "is_reward";
    String RECEIVE_NOMINATE_ACTION = "com.payment.confirm";
    String LAUNCH_FROM = "launch_from";
    String GAME_STATUS = "status";
    String TRANSACTION_ID = "transaction_id";

    String POST_AMOUNT_ENTRY = "post_amount_entry";
    String POST_CARD_PRESENTED = "post_card_presented";
    String POST_TRANSACTION = "post_transaction";



}
