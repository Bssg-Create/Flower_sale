-- Legacy trigger backup captured before the 2026-08-04 service-layer inventory migration.
-- Do not enable this trigger while OrderService deducts stock at order creation,
-- otherwise completing an order would deduct the same inventory a second time.

DELIMITER //
CREATE TRIGGER update_order_status AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF NEW.status = 'completed' THEN
        UPDATE flower f
        JOIN order_item oi ON f.id = oi.flower_id
        SET f.stock = f.stock - oi.quantity
        WHERE oi.order_id = NEW.id;
    END IF;
END //
DELIMITER ;
