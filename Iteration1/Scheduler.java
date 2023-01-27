package Iteration1;

import java.util.ArrayList;

public class Scheduler {
    private boolean empty = true;
    private ArrayList<Integer> ingredients = new ArrayList<>();
    private int totalSandwhiches = 0;

    /**
     * The getter method for the Table class gets items from {@link #ingredients}
     * that were passed from the Agent and gives them to the 
     * correct Chef (Jam, Bread, or Peanut Butter)
     * @param ingredient the ingredient that the Chef supplies
     * (1 - bread, 2 - jam, 3 - peanut butter)
     * @return the ingredients that the Agent passed and stored on the Table
     */
    public synchronized ArrayList<Integer> get(int ingredient) {
        while (empty || isRightIngredient(ingredient)) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        ArrayList<Integer> buffer = new ArrayList<>();
        buffer.addAll(ingredients);
        ingredients.clear();
        empty = true;
        totalSandwhiches++;
        System.out.println("Total sandwhiches made: " + totalSandwhiches + "\n");
        if (totalSandwhiches >= 20) {
            System.exit(1);
        }
        notifyAll();
        return buffer;
    }

    /**
     * The putter method for the Table class puts the items that were
     * passed from Agent into {@link #ingredients}, missing one of the
     * three ingredients. Both ingredients are randomly gernerated numbers
     * @param firstIngredient the first ingredient that the Agent supplies
     * @param secondIngredient the second ingredient that the Agent supplies
     */
    public synchronized void put(int firstIngredient, int secondIngredient) {
        while(!empty) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.ingredients.add(firstIngredient);
        this.ingredients.add(secondIngredient);
        empty = false;
        notifyAll();
    }

    /**
     * Helper method isRightIngredient checks {@link #ingredients}
     * to see if the ingredient being passed is the correct ingredient
     * that is missing.
     * @param ingredient the ingredient that is attempting to be passed to finish the sandwhich
     * @return true if ingredient is in {@link #ingredients}, false otherwise
     */
    private boolean isRightIngredient(int ingredient) {
        if (this.ingredients.get(0) == ingredient || this.ingredients.get(1) == ingredient) {
            return true;
        }
        return false;
    }
}
