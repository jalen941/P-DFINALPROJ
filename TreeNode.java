

class TreeNode {
    int[][] matrix;
    TreeNode left;
    TreeNode right;

    public TreeNode(int[][] matrix) {
        this.matrix = matrix;
    }

    public TreeNode(TreeNode left, TreeNode right) {
        this.left = left;
        this.right = right;
    }

    // Check if the node is a leaf node
    public boolean isLeaf() {
        return left == null && right == null;
    }
}
